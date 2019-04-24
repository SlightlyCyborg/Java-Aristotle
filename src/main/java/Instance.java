public class Instance {
    Searcher searcher;
    Indexer indexer;
    Renderer renderer;

    private String name, username;

    String getName(){
        return name;
    }

    void setName(String name){
        this.name = name;
    }

    String getUsername(){
        return username;
    }

    void setUsername(String username){
        this.username = username;
    }
}
