import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;


public class IndexerProcess extends Thread{
    Queue<Instance> instancesToIndex;
    boolean stopProcessAfterIndexingAllSignaled = false;
    Instance currentInstance;

    public IndexerProcess (){
       instancesToIndex = new LinkedList<>();
    }

    public synchronized IndexProgress progress(){
        Queue<Instance> copyOfRest = new LinkedList<>(instancesToIndex);
        return new IndexProgress(currentInstance, copyOfRest);
    }

    @Override
    public void run(){
        loop();
    }

    private void loop(){
        while(shouldStillRun()){
            if(somethingToIndex()) {
                Instance toBeIndexed = getNextIndexer();
                runIndexer(toBeIndexed);
            } else {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    synchronized public void addInstanceToIndex(Instance instance){
        //called from Server.addInstance()
        instancesToIndex.add(instance);
    }

    synchronized public void stopProcessAfterIndexingAll(){
        stopProcessAfterIndexingAllSignaled = true;
    }

    private synchronized Instance getNextIndexer(){
        return instancesToIndex.poll();
    }

    synchronized private boolean somethingToIndex(){
        return instancesToIndex.size()>0;


    }

    synchronized private boolean shouldStillRun(){
        return !stopProcessAfterIndexingAllSignaled || instancesToIndex.size() > 0;
    }

    private void runIndexer(Instance instance){
        updateProgress(instance);
        try {
            instance.indexer.indexAllSinceDate(LocalDate.now());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        updateProgress(null);
    }

    private synchronized void updateProgress(Instance currentlyRunning){
        currentInstance = currentlyRunning;
    }
}
