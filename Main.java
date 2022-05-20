package gitlet;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Kaihao Wen
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> ....
     *  java gitlet.Main add hello.txt*/
    public static void main(String... args) {
        String[] words = {
                "init", "add", "commit", "rm", "log", "global-log",
                "find", "status", "checkout", "branch", "rm-branch",
                "reset", "merge"
        };
        List<String> commands = Arrays.asList(words);

        if(args.length == 0){
            System.out.println("Please enter a command.");
        }
        else if(!commands.contains(args[0])){
            System.out.println("No command with that name exists.");
        }
        else if(args[0].equals("init")){
            if(Utils.join(".gitlet").isDirectory()){
                System.out.println("A Gitlet version-control system already exists in the current directory.");
                return;
            }
            if(args.length != 1){
                System.out.println("Incorrect operands.");
            }
            init();
        }
        else if(!Utils.join(".gitlet").isDirectory()){
            System.out.println("Not in an initialized Gitlet directory.");
        }
        else if(args[0].equals("add")){
            if(args.length != 2){
                System.out.println("Incorrect operands.");
            }
            add(args[1]);
        }
        else if(args[0].equals("commit")){
            if(args.length == 1){
                System.out.println(" Please enter a commit message.");
            } else if(args.length != 2){
                System.out.println("Incorrect operands.");
            } else {
                commit(args[1], null);
            }
        }
        else if(args[0].equals("log")){
            if(args.length != 1){
                System.out.println("Incorrect operands.");
            }
            log();
        }
        else if(args[0].equals("checkout")){
            if(args.length == 3){
                if(args[1].equals("--")){
                    checkout(1, null, args[2]);
                } else {
                    System.out.println("Incorrect operands.");
                }
            }
            else if(args.length == 4){
                if(args[2].equals("--")){
                    checkout(2, args[1], args[3]);
                }
                else{
                    System.out.println("Incorrect operands.");
                }
            }
            else if(args.length == 2){
                checkout(3, null, args[1]);
            }
            else {
                System.out.println("Incorrect operands.");
            }
        }
        else if(args[0].equals("rm")){
            if(args.length != 2){
                System.out.println("Incorrect operands.");
            }
            rm(args[1]);
        }
        else if(args[0].equals("global-log")){
            if(args.length != 1){
                System.out.println("Incorrect operands.");
            }
            globallog();
        }
        else if(args[0].equals("find")){
            if(args.length != 2){
                System.out.println("Incorrect operands.");
            }
            find(args[1]);
        }
        else if(args[0].equals("status")){
            if(args.length != 1){
                System.out.println("Incorrect operands.");
            }
            status();
        }
        else if(args[0].equals("branch")){
            if(args.length != 2){
                System.out.println("Incorrect operands.");
            }
            branch(args[1]);
        }
        else if(args[0].equals("rm-branch")){
            if(args.length != 2){
                System.out.println("Incorrect operands.");
            }
            rmbranch(args[1]);
        }
        else if(args[0].equals("reset")){
            if(args.length != 2){
                System.out.println("Incorrect operands.");
            }
            reset(args[1]);
        }
        else if(args[0].equals("merge")){
            if(args.length != 2){
                System.out.println("Incorrect operands.");
            }
            merge(args[1]);
        }
    }

    /** Create new Gitlet repository. */
    public static void init(){
        //get CWD
        File cwd = new File (System.getProperty("user.dir"));

        //make repo directory
        File dotgitlet = Utils.join(cwd, ".gitlet");
        if(!dotgitlet.exists()) {
            dotgitlet.mkdir();
        } else {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return;
        }

        //make initial commit
        Commit initial = new Commit("initial commit", null);

        File COMMITS = Utils.join(dotgitlet, "COMMITS");
        if(!COMMITS.exists()){
            COMMITS.mkdir();
        }
        File BLOBS = Utils.join(dotgitlet, "BLOBS");
        if(!BLOBS.exists()){
            BLOBS.mkdir();
        }

        //serialize initial commit to COMMITS folder
        initial.setHashID();
        File initCommitFile = Utils.join(COMMITS, initial.getHashID());
        try {
            initCommitFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(initCommitFile, initial);

        //make branches, initialize master branch pointing to initial commit
        TreeMap<String, String> branches = new TreeMap<>();
        branches.put("master", initial.getHashID());

        //create BRANCHES file
        File BRANCHES = Utils.join(".gitlet/BRANCHES");
        try {
            BRANCHES.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //serialize branches into BRANCHES file
        Utils.writeObject(BRANCHES, branches);

        //create current branch file
        File CURRBRANCH = Utils.join(".gitlet/CURRBRANCH");
        try {
            CURRBRANCH.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.writeContents(CURRBRANCH, "master");

        //make head (String) pointer
        String head = branches.get("master");

        //create HEAD file
        File HEAD = Utils.join(".gitlet/HEAD");
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //serialize head into HEAD file
        Utils.writeContents(HEAD, head);

        //create addition stage folder
        File ADDSTAGE = Utils.join(cwd,".gitlet/ADDSTAGE");
        if(!ADDSTAGE.exists()) {
            ADDSTAGE.mkdir();
        }

        //create addition stage map
        TreeMap<String, String> addstagemap = new TreeMap<>();

        //create ADDSTAGEMAP file
        File ADDSTAGEMAP = Utils.join(".gitlet/ADDSTAGEMAP");
        try {
            ADDSTAGEMAP.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //serialize addstage map to ADDSTAGEMAP file
        Utils.writeObject(ADDSTAGEMAP, addstagemap);

        //create removal ArrayLists
        ArrayList<String> remstage = new ArrayList<>();

        //create removal stage file
        File REMSTAGE = Utils.join(".gitlet/REMSTAGE");
        try {
            REMSTAGE.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //serialize removal stage to REMSTAGE file
        Utils.writeObject(REMSTAGE, remstage);
    }

    /** Stage a file for addition. */
    public static void add(String filename){
        //get filename and ADDSTAGEMAP, file contents as byte[], and hash of file contents
        File f = Utils.join(filename);
        File ADDSTAGEMAP = Utils.join(".gitlet/ADDSTAGEMAP");
        if(!f.exists()){
            System.out.println("File does not exist.");
            return;
        }

        TreeMap<String, String> addstagemap = Utils.readObject(ADDSTAGEMAP, TreeMap.class);
        byte[] fileContents = Utils.readContents(f);
        String hash = Utils.sha1(fileContents);

        //if filename alr exists in ADDSTAGE, delete it
        if(addstagemap.containsKey(filename)){
            String oldhash = addstagemap.get(filename);
            File oldfile = Utils.join(".gitlet/ADDSTAGE", oldhash);
            oldfile.delete();
        }

        //if added file is same as file from most recent commit, don't add it
        String headCommitHash = Utils.readContentsAsString(Utils.join(".gitlet/HEAD"));
        Commit headCommit = Utils.readObject(Utils.join(".gitlet/COMMITS", headCommitHash), Commit.class);
        String recentHash = headCommit.getBlobHash(filename);
        boolean sameAsPrevCommit = false;
        if(recentHash != null && recentHash.equals(hash)){
            sameAsPrevCommit = true;
        }

        if(!sameAsPrevCommit) {
            //make file in ADDSTAGE where snapshot of file will be serialized
            File addstageFile = Utils.join(".gitlet/ADDSTAGE", hash);
            try {
                addstageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //serialize snapshot of file to file in ADDSTAGE
            Utils.writeContents(addstageFile, fileContents);

            //add <filename, hashID> to ADDSTAGEMAP
            addstagemap.put(filename, hash);
        }

        //reserialize addstagemap to ADDSTAGEMAP
        Utils.writeObject(ADDSTAGEMAP, addstagemap);

        //REMOVE FILE FROM REMSTAGE
        File REMSTAGE = Utils.join(".gitlet/REMSTAGE");
        ArrayList<String> remstage = Utils.readObject(REMSTAGE, ArrayList.class);
        if(remstage.contains(filename)) {
            remstage.remove(filename);
            Utils.writeObject(REMSTAGE, remstage);
        }
    }

    /** Make a commit. */
    public static void commit(String msg, String secondParentID){
        //clone HEAD commit
        String headCommitHash = Utils.readContentsAsString(Utils.join(".gitlet/HEAD"));
        Commit C = Utils.readObject(Utils.join(".gitlet/COMMITS", headCommitHash), Commit.class);

        //read ADDSTAGEMAP
        File ADDSTAGEMAP = Utils.join(".gitlet/ADDSTAGEMAP");
        TreeMap<String, String> addstagemap = Utils.readObject(ADDSTAGEMAP, TreeMap.class);

        //read REMSTAGE
        File REMSTAGE = Utils.join(".gitlet/REMSTAGE");
        ArrayList<String> remstage = Utils.readObject(REMSTAGE, ArrayList.class);

        //failure cases
        if(addstagemap.isEmpty() && remstage.isEmpty()){
            System.out.println("No changes added to the commit.");
            return;
        }
        if(msg.length() == 0){
            System.out.println("Please enter a commit message.");
            return;
        }

        //modify commit msg and timestamp according to user input
        C.setMsg(msg);
        C.setTimestamp();

        //set parent to prev commit
        C.setParent(headCommitHash);

        //set second parent if given
        C.setSecondParent(secondParentID);

        //array of filenames in ADDSTAGE
        String[] addedFiles = addstagemap.keySet().toArray(new String[0]);

        //process each added file
        for(String filename : addedFiles){
            //create new blob file for snapshot
            String hash = addstagemap.get(filename);
            File blob = Utils.join(".gitlet/BLOBS", hash);
            try {
                blob.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //copy over content from added file to blob file
            byte[] addedFileContent = Utils.readContents(Utils.join(".gitlet/ADDSTAGE", hash));
            Utils.writeContents(blob, addedFileContent);

            //update tracked files in commit
            C.insertBlob(filename, hash);

            //delete file from addstagemap
            addstagemap.remove(filename);

            //delete file from ADDSTAGE
            if(!addstagemap.containsValue(hash)) {
                try {
                    Files.delete(Utils.join(".gitlet/ADDSTAGE", hash).toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //process each removed file
        for(String r : remstage){
            C.getBlobsMap().remove(r);
        }

        remstage.clear();
        Utils.writeObject(REMSTAGE, remstage);

        //make new commit file in COMMITS and serialize new commit there
        C.setHashID();
        File newCommit = Utils.join(".gitlet/COMMITS", C.getHashID());
        try {
            newCommit.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.writeObject(newCommit, C);

        //update head to newly created commit
        Utils.writeContents(Utils.join(".gitlet/HEAD"), C.getHashID());

        //update current branch to point to HEAD
        File BRANCHES = Utils.join(".gitlet/BRANCHES");
        TreeMap<String, String> branches = Utils.readObject(BRANCHES, TreeMap.class);
        branches.put(getCurrentBranch(), C.getHashID());
        Utils.writeObject(BRANCHES, branches);

        //clear staging area
        addstagemap.clear();
        Utils.writeObject(ADDSTAGEMAP, addstagemap);
    }

    /** Checkout a specific file or a branch. */
    public static void checkout(int mode, String commitID, String name){
        File cwd = new File (System.getProperty("user.dir"));
        File COMMITS = Utils.join(".gitlet/COMMITS");
        File BLOBS = Utils.join(".gitlet/BLOBS");

        //get head commit hash
        File HEAD = Utils.join(".gitlet/HEAD");
        String head = Utils.readContentsAsString(HEAD);

        if(mode == 1){ //checkout [file name]
            //get head Commit obj and find tracked blobID corresponding to filename
            Commit headCommit = Utils.readObject(Utils.join(COMMITS, head), Commit.class);
            String targetBlobID = headCommit.getBlobHash(name);

            //if file doesn't exist in headCommit, abort, and print error msg
            if(targetBlobID == null){
                System.out.println("File does not exist in that commit.");
                return;
            }

            //locate target blob and replace file w/ matching name in cwd
            File targetBlob = Utils.join(BLOBS, targetBlobID);
            try {
                Files.copy(targetBlob.toPath(), Utils.join(cwd, name).toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(mode == 2){ //checkout [commitID] [file name]
            //get target Commit obj and find tracked blobID corresponding to filename
            Commit targetCommit;
            if(commitID.length() < 40){
                commitID = getFullID(commitID);
            }
            try {
                targetCommit = Utils.readObject(Utils.join(COMMITS, commitID), Commit.class);
            } catch (IllegalArgumentException e){
                System.out.println("No commit with that id exists.");
                return;
            }
            String targetBlobID = targetCommit.getBlobHash(name);

            //if file doesn't exist in targetCommit, abort, and print error msg
            if(targetBlobID == null){
                System.out.println("File does not exist in that commit.");
                return;
            }

            //locate target blob and replace file w/ matching name in cwd
            File targetBlob = Utils.join(BLOBS, targetBlobID);
            try {
                Files.copy(targetBlob.toPath(), Utils.join(cwd, name).toPath(), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(mode == 3){
            //get branches treemap
            File BRANCHES = Utils.join(".gitlet/BRANCHES");
            TreeMap<String, String> branches = Utils.readObject(BRANCHES, TreeMap.class);

            //if no branch w/ name exists, error and abort
            if(branches.get(name) == null){
                System.out.println("No such branch exists.");
                return;
            }

            //if name = current branch, abort
            if(name.equals(getCurrentBranch())){
                System.out.println("No need to checkout the current branch.");
                return;
            }

            //get head commit of specified branch
            Commit targetCommit = Utils.readObject(Utils.join(COMMITS, branches.get(name)), Commit.class);

            //array of names of blobs/files tracked by targetCommit
            String[] blobs = targetCommit.getBlobsMap().keySet().toArray(new String[0]);

            //loop through tracked blobs and put/replace them in cwd
            for(String blobname : blobs){
                if(Utils.join(cwd, blobname).isFile() && !isTracked(blobname, head)){
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    return;
                }
                File targetBlob = Utils.join(BLOBS, targetCommit.getBlobHash(blobname));
                try {
                    Files.copy(targetBlob.toPath(), Utils.join(cwd, blobname).toPath(), REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //delete files that are tracked in current branch but not present in checked out branch
            for(String f : Utils.plainFilenamesIn(cwd)){
                if(f != null && isTracked(f, head) && !isTracked(f, targetCommit.getHashID())){
                    Utils.restrictedDelete(f);
                }
            }

            //set head to checked out branch and reserialize head to HEAD
            head = targetCommit.getHashID();
            Utils.writeContents(HEAD, head);

            //set current branch to checked out branch
            Utils.writeContents(Utils.join(".gitlet/CURRBRANCH"), name);

            //clear staging area
            clearAddstage();
        }
    }

    /** Print log of commits in current branch. */
    public static void log(){
        File HEAD = Utils.join(".gitlet/HEAD");
        File COMMITS = Utils.join(".gitlet/COMMITS");
        String head = Utils.readContentsAsString(HEAD);
        Commit commit = Utils.readObject(Utils.join(COMMITS, head), Commit.class);

        while(commit.getParent() != null){
            System.out.println("===");
            System.out.println("commit " + commit.getHashID());
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMsg());
            System.out.println();
            commit = Utils.readObject(Utils.join(COMMITS, commit.getParent()), Commit.class);
        }
        System.out.println("===");
        System.out.println("commit " + commit.getHashID());
        System.out.println("Date: " + commit.getTimestamp());
        System.out.println(commit.getMsg());
        System.out.println();
    }

    /** Stage file for removal. */
    public static void rm(String filename){
        //preliminaries
        boolean added = true;
        File cwd = new File (System.getProperty("user.dir"));
        File ADDSTAGE = Utils.join(".gitlet/ADDSTAGE");
        File ADDSTAGEMAP = Utils.join(".gitlet/ADDSTAGEMAP");
        TreeMap<String, String> addstagemap = Utils.readObject(ADDSTAGEMAP, TreeMap.class);
        File REMSTAGE = Utils.join(".gitlet/REMSTAGE");
        ArrayList<String> remstage = Utils.readObject(REMSTAGE, ArrayList.class);
        File HEAD = Utils.join(".gitlet/HEAD");
        File COMMITS = Utils.join(".gitlet/COMMITS");
        Commit headCommit = Utils.readObject(Utils.join(COMMITS, Utils.readContentsAsString(HEAD)), Commit.class);

        if(addstagemap.get(filename) != null){
            Utils.join(ADDSTAGE, addstagemap.get(filename)).delete();
            addstagemap.remove(filename);
        } else {
            added = false;
        }

        if(headCommit.getBlobHash(filename) != null){
            remstage.add(filename);
            Utils.restrictedDelete(Utils.join(cwd, filename));
        } else {
            if(!added){
                System.out.println("No reason to remove the file.");
            }
        }

        //reserialize
        Utils.writeObject(ADDSTAGEMAP, addstagemap);
        Utils.writeObject(REMSTAGE, remstage);
    }

    /** Finds and prints commit with specified message. */
    public static void find(String msg){
        boolean atLeastOne = false;
        File COMMITS = Utils.join(".gitlet/COMMITS");
        for(String c : Utils.plainFilenamesIn(COMMITS)) {
            Commit commit = Utils.readObject(Utils.join(COMMITS, c), Commit.class);
            if(commit.getMsg().equals(msg)){
                System.out.println(c);
                atLeastOne = true;
            }
        }
        if(!atLeastOne){
            System.out.println("Found no commit with that message.");
        }
    }

    /** Checks out a specified commit and changes the current branch head to that commit. */
    public static void reset(String commitID){
        //preliminaries
        File HEAD = Utils.join(".gitlet/HEAD");
        Commit headCommit = Utils.readObject(Utils.join(".gitlet/COMMITS", Utils.readContentsAsString(HEAD)), Commit.class);
        File cwd = new File (System.getProperty("user.dir"));
        File COMMITS = Utils.join(".gitlet/COMMITS");

        //get full id if abbreviated
        if(commitID.length() < 40){
            commitID = getFullID(commitID);
        }

        //abort if no commitID exists
        if(!Utils.join(COMMITS, commitID).exists()){
            System.out.println("No commit with that id exists.");
            return;
        }

        //get list of blobs from targetCommit
        Commit targetCommit = Utils.readObject(Utils.join(COMMITS, commitID), Commit.class);
        String[] blobs = targetCommit.getBlobsMap().keySet().toArray(new String[0]);

        //iterates thru blobs and checks out all files
        for(String b : blobs){
            if(Utils.join(cwd, b).isFile() && !isTracked(b, headCommit.getHashID())){ //untracked in currentCommit but present in cwd
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            }
            checkout(2, commitID, b);
        }

        //removes tracked files not in commit
        for(String c : Utils.plainFilenamesIn(cwd)){
            if(isTracked(c, headCommit.getHashID()) && !isTracked(c, commitID)){
                Utils.restrictedDelete(Utils.join(cwd, c));
            }
        }

        //move current branch head to commitID
        Utils.writeContents(HEAD, commitID);
        File BRANCHES = Utils.join(".gitlet/BRANCHES");
        TreeMap<String, String> branches = Utils.readObject(BRANCHES, TreeMap.class);
        branches.put(getCurrentBranch(), commitID);
        Utils.writeObject(BRANCHES, branches);

        //clear staging area
        clearAddstage();
    }

    /** Prints status of the repository. */
    public static void status(){
        //preliminaries
        File cwd = new File (System.getProperty("user.dir"));
        //File ADDSTAGE = Utils.join(".gitlet/ADDSTAGE");
        File ADDSTAGEMAP = Utils.join(".gitlet/ADDSTAGEMAP");
        TreeMap<String, String> addstagemap = Utils.readObject(ADDSTAGEMAP, TreeMap.class);
        File REMSTAGE = Utils.join(".gitlet/REMSTAGE");
        ArrayList<String> remstage = Utils.readObject(REMSTAGE, ArrayList.class);
        File HEAD = Utils.join(".gitlet/HEAD");
        Commit headCommit = Utils.readObject(Utils.join(".gitlet/COMMITS", Utils.readContentsAsString(HEAD)), Commit.class);
        File BRANCHES = Utils.join(".gitlet/BRANCHES");
        TreeMap<String, String> branches = Utils.readObject(BRANCHES, TreeMap.class);
        String[] branchNames = branches.keySet().toArray(new String[0]);
        String[] addedFiles = addstagemap.keySet().toArray(new String[0]);
        //String[] cwdFiles = cwd.list();
        Arrays.sort(branchNames);
        Arrays.sort(addedFiles);
        //Arrays.sort(cwdFiles);

        System.out.println("=== Branches ===");
        for(String b : branchNames){
            if(b.equals(getCurrentBranch())){
                System.out.println("*" + b);
            } else {
                System.out.println(b);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for(String a : addedFiles){
            System.out.println(a);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for(String r : remstage){
            System.out.println(r);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        for(String c : Utils.plainFilenamesIn(cwd)){
            File cwdf = Utils.join(cwd, c);
            File trackedf = null;
            if(isTracked(c, headCommit.getHashID())) {
                trackedf = Utils.join(".gitlet/BLOBS", headCommit.getBlobHash(c));
            }
            File addedf = null;
            if(addstagemap.containsKey(c)) {
                addedf = Utils.join(".gitlet/ADDSTAGE", addstagemap.get(c));
            }

            //conditions for modifications not staged for commit
            if(isTracked(c, headCommit.getHashID()) && !(hash(cwdf).equals(hash(trackedf))) && addstagemap.get(c) == null){
                System.out.println(c);
                continue;
            } else if (addstagemap.get(c) != null && !(hash(cwdf).equals(hash(addedf)))){
                System.out.println(c);
                continue;
            } else if (addstagemap.get(c) != null && !cwdf.exists()) {
                System.out.println(c);
                continue;
            } else if (!remstage.contains(c) && isTracked(c, headCommit.getHashID()) && !cwdf.exists()){
                System.out.println(c);
                continue;
            }
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        for(String c : Utils.plainFilenamesIn(cwd)) {
            if(!isTracked(c, headCommit.getHashID()) && addstagemap.get(c) == null){
                System.out.println(c);
            }
        }
        System.out.println();
    }

    /** Prints log of all commits from all branches. */
    public static void globallog(){
        File COMMITS = Utils.join(".gitlet/COMMITS");
        for(String c : Utils.plainFilenamesIn(COMMITS)){
            Commit commit = Utils.readObject(Utils.join(COMMITS, c), Commit.class);

            System.out.println("===");
            System.out.println("commit " + commit.getHashID());
            System.out.println("Date: " + commit.getTimestamp());
            System.out.println(commit.getMsg());
            System.out.println();
        }
    }

    /** Creates a branch and points it at current HEAD node. */
    public static void branch(String name){
        //get branches TreeMap
        File BRANCHES = Utils.join(".gitlet/BRANCHES");
        TreeMap<String, String> branches = Utils.readObject(BRANCHES, TreeMap.class);

        //get headCommit hash
        String headCommitHash = Utils.readContentsAsString(Utils.join(".gitlet/HEAD"));

        //add new branch w name to branches
        if(branches.get(name) != null){
            System.out.println("A branch with that name already exists.");
            return;
        }
        branches.put(name, headCommitHash);

        //reserialize branches
        Utils.writeObject(BRANCHES, branches);
    }

    /** Removes a specified branch. */
    public static void rmbranch(String name){
        //get branches TreeMap
        File BRANCHES = Utils.join(".gitlet/BRANCHES");
        TreeMap<String, String> branches = Utils.readObject(BRANCHES, TreeMap.class);

        //error if branch w name dne
        if(branches.get(name) == null){
            System.out.println("A branch with that name does not exist.");
            return;
        }

        //abort if name == current branch/head
        String headCommitHash = Utils.readContentsAsString(Utils.join(".gitlet/HEAD"));
        if(headCommitHash.equals(branches.get(name))){
            System.out.println("Cannot remove the current branch.");
            return;
        }

        //delete branch pointer w/ name
        branches.remove(name);

        //reserialize branches
        Utils.writeObject(BRANCHES, branches);
    }

    /** Merges target branch into current branch. */
    public static void merge(String targetBranch){
        //preliminaries
        File cwd = new File (System.getProperty("user.dir"));
        File COMMITS = Utils.join(".gitlet/COMMITS");
        File BLOBS = Utils.join(".gitlet/BLOBS");
        File ADDSTAGE = Utils.join(".gitlet/ADDSTAGE");
        File ADDSTAGEMAP = Utils.join(".gitlet/ADDSTAGEMAP");
        TreeMap<String, String> addstagemap = Utils.readObject(ADDSTAGEMAP, TreeMap.class);
        File REMSTAGE = Utils.join(".gitlet/REMSTAGE");
        ArrayList<String> remstage = Utils.readObject(REMSTAGE, ArrayList.class);
        File HEAD = Utils.join(".gitlet/HEAD");
        File BRANCHES = Utils.join(".gitlet/BRANCHES");
        TreeMap<String, String> branches = Utils.readObject(BRANCHES, TreeMap.class);
        String[] branchNames = branches.keySet().toArray(new String[0]);
        String[] addedFiles = addstagemap.keySet().toArray(new String[0]);
        String[] cwdFiles = cwd.list();
        Arrays.sort(branchNames);
        Arrays.sort(addedFiles);
        Arrays.sort(cwdFiles);
        boolean hasConflict = false;
        //failure cases
        if(!addstagemap.isEmpty() || !remstage.isEmpty()){
            System.out.println("You have uncommitted changes.");
            return;
        }
        if(!branches.containsKey(targetBranch)){
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if(targetBranch.equals(getCurrentBranch())){
            System.out.println("Cannot merge a branch with itself.");
            return;
        }

        //get three needed Commits
        Commit currentCommit = Utils.readObject(Utils.join(COMMITS, Utils.readContentsAsString(HEAD)), Commit.class);
        Commit targetCommit = Utils.readObject(Utils.join(COMMITS, branches.get(targetBranch)), Commit.class);
        Commit splitCommit = Utils.readObject(Utils.join(COMMITS, identifySplits(currentCommit.getHashID(), targetCommit.getHashID())), Commit.class);

        //special cases:
        if(splitCommit.getHashID().equals(targetCommit.getHashID())){
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if(splitCommit.getHashID().equals(currentCommit.getHashID())){
            //check out given branch
            for(String b : branchNames){
                if(branches.get(b).equals(targetCommit.getHashID())){
                    checkout(3, null, b);
                }
            }
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        for(String s : targetCommit.getBlobsMap().keySet().toArray(new String[0])) {
            String splitHash = splitCommit.getBlobHash(s);
            String targetHash = targetCommit.getBlobHash(s);
            String currentHash = currentCommit.getBlobHash(s);
            if (splitHash == null && targetHash == null && currentHash != null) {
                //case 4 --> if cond is unreachable but we do nothing for this case anyways
                //do nothing
                continue;
            }
            if (splitHash == null && currentHash == null && targetHash != null) {
                //case 5
                //see case 1
                if(Utils.join(cwd, s).isFile()){ //untracked in currentCommit but present in cwd
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    return;
                }
                checkout(2, targetCommit.getHashID(), s);
                add(s);
                continue;
            }
            //case 7: unmodified in Target but not present in Current -> remain removed
            if (splitHash != null && targetHash != null && currentHash == null){
                if (splitHash.equals(targetHash) && !isTracked(s, currentCommit.getHashID())) {
                    continue;
                }
            }

            //case 3: modified in Target and Current...
            if(splitHash == null){
                splitHash = "";
            }
            if(!splitHash.equals(targetHash) && !splitHash.equals(currentHash)){
                //3a: ... in the same way -> do nothing
                if(targetHash.equals(currentHash)){
                    //do nothing
                    continue;
                } else {
                    //3b: ... in diff ways -> conflict
                    hasConflict = true;
                    String currentContent = Utils.readContentsAsString(Utils.join(BLOBS, currentHash));
                    String targetContent = Utils.readContentsAsString(Utils.join(BLOBS, targetHash));
                    Utils.writeContents(Utils.join(cwd, s), "<<<<<<< HEAD" + "\n" + currentContent + "=======" + "\n" + targetContent + ">>>>>>>" + "\n");
                    add(s);
                    continue;
                }
            }
            if(splitHash.equals("")){
                splitHash = null;
            }

            //case 1: modified in Target but not in Current -> Target
            if(!splitHash.equals(targetHash) && splitHash.equals(currentHash)){
                //checkout file s from targetCommit and stage it
                checkout(2, targetCommit.getHashID(), s);
                add(s);
                continue;
            }

            //case 2: modified in Current but not in Target -> Current
            if(splitHash.equals(targetHash) && !splitHash.equals(currentHash)){
                //do nothing
                continue;
            }
            //case 4: not in Split nor Target but in Current -> Current
                //see case 2

            //case 5: not in Split nor Current but in Target -> Target
                //see case 1

            //case 7: unmodified in Target but not present in Current -> remain removed
                //do nothing
        }

        //check for case 6
        //check for case where removed in Target + modified in Current -> conflict
        for(String s : currentCommit.getBlobsMap().keySet().toArray(new String[0])){
            String splitHash = splitCommit.getBlobHash(s);
            String targetHash = targetCommit.getBlobHash(s);
            String currentHash = currentCommit.getBlobHash(s);

            if (splitHash != null && targetHash == null && currentHash != null){
                //case 6: unmodified in Current but not present in Target -> remove
                if (splitHash.equals(currentHash)) {
                    rm(s);
                    continue;
                }
                //removed in Target + modified in Current -> conflict
                else {
                    hasConflict = true;
                    String currentContent = Utils.readContentsAsString(Utils.join(BLOBS, currentHash));
                    String targetContent = "";
                    Utils.writeContents(Utils.join(cwd, s), "<<<<<<< HEAD" + "\n" + currentContent + "=======" + "\n" + targetContent + ">>>>>>>" + "\n");
                    add(s);
                }
            }
        }

        commit("Merged " + targetBranch + " into " + getCurrentBranch() + ".", targetCommit.getHashID());

        if(hasConflict){
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Given the hashIDs of two commits, return the hashID of their split point.*/
    public static String identifySplits(String currentCommit, String targetCommit){
        ArrayList<String> ancestors = new ArrayList<>();
        ancestors.add(targetCommit);
        LinkedList<String> toVisit = new LinkedList<String>();
        Commit target = Utils.readObject(Utils.join(".gitlet/COMMITS", targetCommit), Commit.class);
        if(target.getParent() != null) {
            toVisit.add(target.getParent());
        }
        if(target.getSecondParent() != null){
            toVisit.add(target.getSecondParent());
        }

        while(!toVisit.isEmpty()){
            String hashID = toVisit.pop();
            ancestors.add(hashID);

            Commit commit = Utils.readObject(Utils.join(".gitlet/COMMITS", hashID), Commit.class);
            if(commit.getParent() != null){
                toVisit.add(commit.getParent());
            }
            if(commit.getSecondParent() != null){
                toVisit.add(commit.getSecondParent());
            }
        }

        Commit current = Utils.readObject(Utils.join(".gitlet/COMMITS", currentCommit), Commit.class);
        //toVisit.add(current.getHashID());
        if(ancestors.contains(current.getHashID())){
            return current.getHashID();
        }
        if(current.getParent() != null) {
            toVisit.add(current.getParent());
        }
        if(current.getSecondParent() != null){
            toVisit.add(current.getSecondParent());
        }
        while(!toVisit.isEmpty()){
            String hashID = toVisit.pop();
            current = Utils.readObject(Utils.join(".gitlet/COMMITS", hashID), Commit.class);
            if(ancestors.contains(hashID)){
                return hashID;
            }
            if(current.getParent() != null) {
                toVisit.add(current.getParent());
            }
            if(current.getSecondParent() != null){
                toVisit.add(current.getSecondParent());
            }
        }

        return null;
    }


    /** Returns whether file with filename is tracked in specified commit */
    public static boolean isTracked(String filename, String commitID){
        Commit commit = Utils.readObject(Utils.join(".gitlet/COMMITS", commitID), Commit.class);
        return commit.getBlobHash(filename) != null;
    }

    /** Returns the hash of the file contents of f*/
    public static String hash(File f){
        byte[] fileContents = Utils.readContents(f);
        return Utils.sha1(fileContents);
    }

    /** Clears staging area */
    public static void clearAddstage(){
        File ADDSTAGE = Utils.join(".gitlet/ADDSTAGE");
        File ADDSTAGEMAP = Utils.join(".gitlet/ADDSTAGEMAP");
        File[] addstageFiles = ADDSTAGE.listFiles();
        for(File f : addstageFiles){
            if(f != null){
                f.delete();
            }
        }
        TreeMap<String, String> addstagemap = Utils.readObject(ADDSTAGEMAP, TreeMap.class);
        addstagemap.clear();
        Utils.writeObject(ADDSTAGEMAP, addstagemap);
    }

    /** Returns name of current branch. */
    public static String getCurrentBranch(){
        File CURRBRANCH = Utils.join(".gitlet/CURRBRANCH");
        return Utils.readContentsAsString(CURRBRANCH);
    }

    /** Returns full commitID corresponding to an abbreviated commitID. */
    public static String getFullID(String abbrevID){
        List<String> commits = Utils.plainFilenamesIn(Utils.join(".gitlet/COMMITS"));
        String closest = "";
        for(String c : commits){
            if(abbrevID.compareTo(c) == -(40 - abbrevID.length())){
                return c;
            }
        }
        return "";
    }
}
