<!DOCTYPE html>
<html lang="en">
<head>
    <title>Search ${instance.name}</title>

    <link rel="stylesheet"
          href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css"
          integrity="sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M"
          crossorigin="anonymous">

    <link href="css/menu.css" rel="stylesheet">
    <link href="css/page.css" rel="stylesheet">
    <link href="imgs/${instance.username}/logo" rel="icon">

    <script type="text/javascript" id="www-widgetapi-script" src="https://s.ytimg.com/yts/jsbin/www-widgetapi-vflf9U9oY/www-widgetapi.js" async=""></script><script src="https://www.youtube.com/iframe_api"></script>
    <script>
        var to_execute = [];
        document.addEventListener('DOMContentLoaded', function() {

            document.getElementById('search').focus();
            document.getElementById('search').select();
            var player_controls = document.getElementsByClassName('player-control');
            console.log('len' + player_controls.length);
            for(var i = 0; i < player_controls.length; i++) {
                var player_control = player_controls[i];
                player_control.onclick = function() {
                    to_execute.push(this);
                    return false;
                };
            }
        });

    </script>
    <meta content="width=device-width, initial-scale=1.0" name="viewport">

</head>

<body>

<a href="${instance.backButtonURL}">
    <button class="btn btn-secondary" type="button">${instance.backButtonText}</button>
</a>
<div class="container" id="head-container">
    <div class="row">
        <a id="header-img-atag">
            <img id="daemon-img" src="${instance.imgSource}"></a>
    </div>
    <div class="row search-bar-row">
        <form class="search-form" method="GET">
            <div class="form-group">
            <#if terms??>
                <input class="form-control" id="search" name="terms" value="${terms}" placeholder="${instance.searchBarText}" type="text">
			<#else>
                <input class="form-control" id="search" name="terms" placeholder="${instance.searchBarText}" type="text">
			</#if>
            </div>
            <div class="form-group">
                <button class="btn btn-lg btn-primary" type="submit">Search</button>
            </div>
        </form>
    </div>
</div>
<div id="results-div">
    <#if hasResults>
        <#include "search.ftl">
    </#if>
</div>
<script src="js/jquery.js"></script>
<script src="js/search.js"></script>
</body>

</html>