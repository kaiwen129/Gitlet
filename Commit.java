package gitlet;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

public class Commit implements Serializable {
    private String msg;
    private String timestamp;
    private String hashID; //do we need this if it get always be generated?
    private TreeMap<String, String> blobs = new TreeMap<>(); //<key, value> = <blob name, blob hashID>

    //List<String> files; list of hashcodes/ids of files tracked
    private String parent; //the hashID of the parent commit
    private String secondParent;

    public Commit(String msg, String parent){
        this.msg = msg;
        this.parent = parent;

        Date d = new Date();
        //initial commit
        if(parent == null){
            d = new Date(0);
        }
        this.timestamp = String.format("%1$ta %1$th %1$td %1$tT %1$tY %1$tz", d);
    }

    public void setHashID(){
        //generate hashID
        ArrayList<Object> hasharr = new ArrayList<>();
        if(msg != null){
            hasharr.add(msg);
        }
        if(parent != null){
            hasharr.add(parent);
        }
        if(timestamp != null){
            hasharr.add(timestamp);
        }
        if(blobs != null){
            hasharr.add(Utils.serialize(blobs));
        }
        if(secondParent != null){
            hasharr.add(secondParent);
        }
        this.hashID = Utils.sha1(hasharr);
    }

    public String getMsg(){
        return this.msg;
    }

    public void setMsg(String msg){
        this.msg = msg;
    }

    public String getTimestamp(){
        return this.timestamp;
    }

    public void setTimestamp(){
        //set timestamp to time when this method was called
        Date d = new Date();
        this.timestamp = String.format("%1$ta %1$th %1$td %1$tT %1$tY %1$tz", d);
    }

    public String getHashID(){
        return this.hashID;
    }

    public String getParent(){
        return this.parent;
    }

    public String getSecondParent(){
        return this.secondParent;
    }

    public void setParent(String parentCommitID){
        this.parent = parentCommitID;
    }

    public void setSecondParent(String secondParentID){
        this.secondParent = secondParentID;
    }

    public TreeMap<String, String> getBlobsMap(){
        return this.blobs;
    }

    public String getBlobHash(String filename){
        return this.blobs.get(filename);
    }

    //update snapshotID tracked by blobs, if not tracked yet, insert new Blob into blobs (start tracking new file)
    public void insertBlob(String filename, String hashID){
        blobs.put(filename, hashID);
    }
}
