package org.jasig.cas.adaptors.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.ConfigurableHashService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;
import org.apache.shiro.util.ByteSource;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.util.Map;

public class ValidUserQueryDBAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    private static final String DEFAULT_PASSWORD_FIELD = "password";
    private static final String DEFAULT_SALT_FIELD = "salt";
    private static final String DEFAULT_NUM_ITERATIONS_FIELD = "numIterations";
    private static final String DEFAULT_LOCKED_FIELD = "locked";
    private static final String DEFAULT_DISABLED_FIELD = "disabled";
    /**
     * The Algorithm name.
     */
    @NotNull
    protected final String algorithmName;

    /**
     * The Sql statement to execute.
     */
    @NotNull
    protected final String sql;

    /**
     * The Password field name.
     */
    @NotNull
    protected String passwordFieldName = DEFAULT_PASSWORD_FIELD;

    /**
     * The Salt field name.
     */
    @NotNull
    protected String saltFieldName = DEFAULT_SALT_FIELD;

    /**
     * The Number of iterations field name.
     */
    @NotNull
    protected String numberOfIterationsFieldName = DEFAULT_NUM_ITERATIONS_FIELD;

    /**
     * The disabled field name.
     */
    @NotNull
    protected String disabledFieldName = DEFAULT_DISABLED_FIELD;

    /**
     * The locked field name.
     */
    @NotNull
    protected String lockedFieldName = DEFAULT_LOCKED_FIELD;

    /**
     * The number of iterations. Defaults to 0.
     */
    protected long numberOfIterations;

    /**
     * The static/private salt.
     */
    protected String staticSalt;


    /**
     * Instantiates a new Query and encode database authentication handler.
     *
     * @param datasource    The database datasource
     * @param sql           the sql query to execute which must include a parameter placeholder
     *                      for the user id. (i.e. <code>SELECT * FROM table WHERE username = ?</code>
     * @param algorithmName the algorithm name (i.e. <code>MessageDigestAlgorithms.SHA_512</code>)
     */

    public ValidUserQueryDBAuthenticationHandler(final DataSource datasource,
                                                 final String sql,
                                                 final String algorithmName) {
        super();
        setDataSource(datasource);
        this.sql = sql;
        this.algorithmName = algorithmName;
    }

    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential)
            throws GeneralSecurityException, PreventedException {
        final String username = getPrincipalNameTransformer().transform(transformedCredential.getUsername());

        try {
            final Map<String, Object> values = getJdbcTemplate().queryForMap(this.sql, username);

            if (Boolean.TRUE.equals(values.get(this.disabledFieldName))) {
                throw new AccountDisabledException(username + "  has been disabled.");
            }
            if (Boolean.TRUE.equals(values.get(this.lockedFieldName))) {
                throw new AccountLockedException(username + "  has been locked.");
            }

            final String digestedPassword = digestEncodedPassword(transformedCredential.getPassword(), values);
            if (!values.get(this.passwordFieldName).equals(digestedPassword)) {
                throw new FailedLoginException("Password does not match value on record.");
            }
            return createHandlerResult(transformedCredential,
                    new SimplePrincipal(username), null);

        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            } else {
                throw new FailedLoginException("Multiple records found for " + username);
            }
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }

    }

    /**
     * Digest encoded password.
     *
     * @param encodedPassword the encoded password
     * @param values          the values retrieved from database
     * @return the digested password
     */
    protected String digestEncodedPassword(final String encodedPassword, final Map<String, Object> values) {
        final ConfigurableHashService hashService = new DefaultHashService();

        if (StringUtils.isNotBlank(this.staticSalt)) {
            hashService.setPrivateSalt(ByteSource.Util.bytes(this.staticSalt));
        }
        hashService.setHashAlgorithmName(this.algorithmName);

        Long numOfIterations = this.numberOfIterations;
        if (values.containsKey(this.numberOfIterationsFieldName)) {
            final String longAsStr = values.get(this.numberOfIterationsFieldName).toString();
            numOfIterations = Long.valueOf(longAsStr);
        }

        hashService.setHashIterations(numOfIterations.intValue());
        if (!values.containsKey(this.saltFieldName)) {
            throw new RuntimeException("Specified field name for salt does not exist in the results");
        }

        final String dynaSalt = values.get(this.saltFieldName) == null ? "" : values.get(this.saltFieldName).toString();
        final HashRequest request = new HashRequest.Builder()
                .setSalt(dynaSalt)
                .setSource(encodedPassword)
                .build();
        return hashService.computeHash(request).toHex();
    }

    /**
     * Gets and sets the real numberOfIterations
     *
     * @return
     */
    public long getAndSetRealNumberOfIterations(final Map<String, Object> values) {
        if (values.containsKey(this.numberOfIterationsFieldName)) {
            final String longAsStr = values.get(this.numberOfIterationsFieldName).toString();
            numberOfIterations = Long.valueOf(longAsStr);
        }
        return numberOfIterations;

    }

    /**
     * Sets static/private salt to be combined with the dynamic salt retrieved
     * from the database. Optional.
     * <p>
     * <p>If using this implementation as part of a password hashing strategy,
     * it might be desirable to configure a private salt.
     * A hash and the salt used to compute it are often stored together.
     * If an attacker is ever able to access the hash (e.g. during password cracking)
     * and it has the full salt value, the attacker has all of the input necessary
     * to try to brute-force crack the hash (source + complete salt).</p>
     * <p>
     * <p>However, if part of the salt is not available to the attacker (because it is not stored with the hash),
     * it is much harder to crack the hash value since the attacker does not have the complete inputs necessary.
     * The privateSalt property exists to satisfy this private-and-not-shared part of the salt.</p>
     * <p>If you configure this attribute, you can obtain this additional very important safety feature.</p>
     *
     * @param staticSalt the static salt
     */
    public final void setStaticSalt(final String staticSalt) {
        this.staticSalt = staticSalt;
    }

    /**
     * Sets password field name. Default is {@link #DEFAULT_PASSWORD_FIELD}.
     *
     * @param passwordFieldName the password field name
     */
    public final void setPasswordFieldName(final String passwordFieldName) {
        this.passwordFieldName = passwordFieldName;
    }

    /**
     * Sets salt field name. Default is {@link #DEFAULT_SALT_FIELD}.
     *
     * @param saltFieldName the password field name
     */
    public final void setSaltFieldName(final String saltFieldName) {
        this.saltFieldName = saltFieldName;
    }

    /**
     * Sets number of iterations field name. Default is {@link #DEFAULT_NUM_ITERATIONS_FIELD}.
     *
     * @param numberOfIterationsFieldName the password field name
     */
    public final void setNumberOfIterationsFieldName(final String numberOfIterationsFieldName) {
        this.numberOfIterationsFieldName = numberOfIterationsFieldName;
    }

    /**
     * Sets disabled field name. Default is {@link #DEFAULT_DISABLED_FIELD}.
     *
     * @param disabledFieldName the disabled field name
     */
    public final void setDisabledFieldName(final String disabledFieldName) {
        this.disabledFieldName = disabledFieldName;
    }

    /**
     * Sets locked field name. Default is {@link #DEFAULT_LOCKED_FIELD}.
     *
     * @param lockedFieldName the locked field name
     */
    public final void setLockedFieldName(final String lockedFieldName) {
        this.lockedFieldName = lockedFieldName;
    }

    /**
     * Sets number of iterations. Default is 0.
     *
     * @param numberOfIterations the number of iterations
     */
    public final void setNumberOfIterations(final long numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }
}
