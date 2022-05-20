# Gitlet Design Document
author: Kaihao Wen

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

### Main.java
This class sets up the persistent file system upon initialization and implements 
the methods of Gitlet.

####Fields
1. String HEAD: Hashcode of the commit referenced by the head pointer.
2. List\<Blob> additionStage: List of Blobs staged for addition
3. List\<Blob> removalStage: List of Blobs staged for removal
4. List\<String> branches: List of hashcodes of branches. Branches are pointers to the most recent commit in 
their corresponding branch.

### Commit.java
This class represents a commit instance. A commit contains metadata, such as a log
message, a commit date, a mapping of file names to blob references, and references 
to parent Commit(s).

####Fields
1. String msg: A string containing the log message.
2. String timestamp: A string containing the commit date and time.
3. String hashID: A string containing the hashcode of the commit.
4. String parent: HashID of parent Commit (null if initial Commit)
5. String secondParent: HashID of merged parent (null if no merge)
6. TreeMap blobs: A map of names to HashID's of Blobs contained in this commit.

### Blob.java (prob don't need)
A representation of the contents of a file.

#### Fields
1. String name: Name of file.
2. String hash: Hashcode of file.
3. File file: The file represented by the Blob.

directory struct:
- .gitlet
  - HEAD file => contains serialized commit hashID pointed to by HEAD pointer
  - CURRBRANCH file => contains serialized name of current branch
  - BRANCHES file => contains serialized TreeMap<String branch_name, String [hashID of latest commit pointed to by branch]>
  - ADDSTAGE folder => contains serialized snapshots of files in CWD staged for addition
  - ADDSTAGEMAP file => TreeMap mapping file names to snapshotIDs in ADDSTAGE folder
  - REMSTAGE file => contains serialized ArrayList<String> of names of files in CWD staged for removal
  - COMMITS folder => contains individual commit files
    - COMMIT file => contains serialized Commit obj, name of file is hashID of commit
  - BLOBS folder => contains individual files
    - BLOB file => contains serialized snapshot of a file in the CWD, name of blob file is hashID of contents of target file


## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.

####add(filename)
1. Create File tracking filename (error if DNE).
2. Create byte[] of file contents and hashID of file based on file contents
3. Create file in ADDSTAGE (name: hashID) and serialize snapshot of file contents to it
4. Add <filename, hashID> to ADDSTAGEMAP
5. 

#### Two ways to create snapshot of a file
1. byte[] content = Utils.readContents(originalFile);
   Utils.writeContents(snapshotFile, content); 
2. Files.copy(originalFile.toPath(), snapshotFile.toPath(), REPLACE_EXISTING);

####

### Main
1. main(String[] args): Beginning of the program, takes in commands along with their
arguments to perform actions.
2. init(): Creates a new repository and a persistent file system.
3. commit(String msg): Creates a new Commit with log message msg.
4. rm(String name): Remove file with given name.
5. log(): Returns a log of commit history.
6. global-log(): Returns a log of all commits ever made.
7. find(String msg): Returns the ids of all commits with log message msg.
8. status(): Logs existing branches and staged files.
9. checkout(args): Replaces file(s) in working directory with those in the specified
commit.
10. branch(String name): Creates new branch with name, pointing it at the current HEAD.
11. rm-branch(String name): Deletes branch with name
12. reset(id): Replaces entire working directory with specified commit.
13. merge(String name): Merges given branch with name to current branch.

## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

###-add [filename]
Adds file with filename to staging area and adds it to a folder for storage.

###-commit
Creates a new commit object, using Java's File system to serialize current snapshot
to a file in repository. 

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

