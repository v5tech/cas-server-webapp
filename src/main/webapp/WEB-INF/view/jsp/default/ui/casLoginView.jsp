<!doctype html>
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <meta charset="utf-8">
    <title>欢迎登录</title>

    <link href="css/login.css" type="text/css" rel="stylesheet">

    <script src="js/jquery-1.8.3.min.js"></script>

    <script>

        function fvh() {
            var vh = $(window).height();
            $(".index_login").height(vh);
        }
        $(function () {
            fvh();
        })
        $(window).resize(function () {
            fvh();
        })

        $(function () {
            $(".re_password .rem_btn").click(function () {

                if ($(this).find("img").css("margin-top") == "0px") {
                    $(this).find("img").css("margin-top", "-30px");
                    $('#rememberMe').val('false')
                }
                else {
                    $(this).find("img").css("margin-top", "0px");
                    $('#rememberMe').val('true')
                }
            })
        })

    </script>
</head>

<body>

<div class="index_login">
    <div class="outer_box">
        <div class="big_logo"></div>
        <h1><img src="images/login_wel_txt.png"></h1>
        <div class="login_box">
            <form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true">
                <div class="w261">
                    <div class="user">
                        <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
                        <form:input cssClass="input_box" placeholder="请输入用户名" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" />
                    </div>
                    <div class="user">
                        <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
                        <form:password cssClass="input_box password" placeholder="请输入密码"  id="password" size="25" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                    </div>

                    <div class="re_password">
                        <!--
                        <p><a href="#">忘记密码？点击重置</a></p>
                        -->
                        <div class="remember">
                            <span>记住登录状态</span>
                            <div class="rem_btn">
                                <img src="images/rem_btn.png">
                            </div>
                            <input type="hidden" name="rememberMe" id="rememberMe" value="true"/>
                        </div>
                    </div>

                    <c:if test="${not empty count && count >= 3}">
                        <div class="verify">
                            <div class="verify_box">
                                <spring:message code="screen.welcome.label.captcha.accesskey" var="authcaptchaAccessKey" />
                                <input class="input_box" type="text" placeholder="请输入验证码" maxlength="10" name="captcha">
                                <input type="hidden" name="showCaptcha" value="true"/>
                            </div>
                            <div class="ver_img">
                                <img alt="<spring:message code="required.captcha" />"
                                     onclick="this.src='captcha.jpg?'+Math.random()" src="captcha.jpg">
                            </div>
                        </div>
                    </c:if>

                    <a href="#"><button class="button" type="submit">立即登录</button></a>
                    <form:errors path="*" id="msg" cssClass="alert_tip" element="div" htmlEscape="false"/>
                    <input type="hidden" name="lt" value="${loginTicket}"/>
                    <input type="hidden" name="execution" value="${flowExecutionKey}"/>
                    <input type="hidden" name="_eventId" value="submit"/>
                </div>
                <!--w261_end-->
            </form:form>
        </div><!--login_box_end-->
    </div><!--outer_box_end-->
</div>
</body>
</html>
