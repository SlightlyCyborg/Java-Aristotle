<html>
    <head>
        <link rel="stylesheet"
              href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css"
              integrity="sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M"
              crossorigin="anonymous">
    </head>
    <body>
        <h1>Add an Instance</h1>
        <form method="post" action="/admin/add-instance">
            <div class="form-group">
                <label>Username:</label>
                <input name="username" type="text">
            </div>
            <div class="form-group">
                <label>Display Name: </label>
                <input name="name" type="text">
            </div>
            <div class="form-group">
                <label>YouTube URL: <input name="youtubeUrl" type="text"></label>
            </div>
            <div class="form-group">
                <label>Back Button Text: <input name="backButtonText" type="text"></label>
            </div>
            <div class="form-group">
                <label>Back Button Url: <input name="backButtonUrl" type="text"></label>
            </div>
            <div class="form-group">
                <label>Search Bar Text:<input name="searchBarText" type="text"></label>
            </div>
            <div class="form-group">
                <label>password:<input name="password" type="text"></label>
            </div>
            <div class="form-group">
                <button type="submit">Add Instance Now</button>
            </div>
        </form>
    </body>
</html>