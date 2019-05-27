<!DOCTYPE html>
<html lang="en">
    <head>
        <link rel="stylesheet"
              href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css"
              integrity="sha384-/Y6pD6FV/Vv2HJnA6t+vslU6fwYXjCFtcEpHbNJ0lyAFsXTsjBbfaDjzALeQsN6M"
              crossorigin="anonymous">
    </head>
    <bod>
        <h1>Admin</h1>
        <button  onclick="window.location.href = '/admin/add-instance';">Add an Instance</button>
        <button  onclick="window.location.href = '/admin/indexer-progress';">Indexer Progress</button>
        <h2>Instances</h2>
        <ul>
            <#list instances as instance>
                <li>${instance.name}:${instance.username}
                    <button>View</button>
                    <button>Edit</button>
                    <button>Deactivate</button>
                </li>
            </#list>
        </ul>
    </bod>
</html>
