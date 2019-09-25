# Aristotle: Search YouTube channels based on captions.
Generates a list of timestamps with search results.

![Search](https://github.com/slightlycyborg/Java-Aristotle/raw/master/vlogbrothers.png "YCombinator Search")

## Dependencies
A solr server running with 2 cores `blocks` & `videos`. `test-blocks` & `test-videos` for testing
To create a solr core visit `localhost:8983/solr/`. Click core admin. Add core.
Change name and instance dir to `blocks` and then `videos. Default schema is fine.

## How to run from jar
```
cd deploy
./run
```

## How to run from IDE
Set the run configuration to use deploy as the workspace.
Run the configuration
