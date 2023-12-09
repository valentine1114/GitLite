import Exceptions.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 主程序
 * 1.利用equals()/switch等方法判断字符串，执行对应命令；
 * 2.args[]长度为0或格式错误时打印提示信息。
 */

public class git {

    public static void main(String[] args) throws Exception {


        if (args.length == 0) {
            System.err.println("Please enter a command.");
            return;
        }


        if (args[0].equals("init")) {
            init(args);

        } else {
            if (!isInitialized()) {
                System.err.println("Not in an initialized git.Mygit directory.");
                System.exit(0);
            }

            switch (args[0]) {
                case "add":
                    add(args);
                    break;
                case "commit":
                    commit(args);
                    break;
                case "rm":
                    rm(args);
                    break;
                case "log":
                    log(args);
                    break;
                case "reset":
                    reset(args);
                    break;
                case "pull":
                    pull(args);
                    break;
                case "push":
                    push(args);
                    break;
                case "diff":
                    diff(args);
                    break;
                case "branch":
                    branch(args);
                    break;
                case "status":
                    status(args);
                    break;
                case "checkout":
                    checkout(args);
                    break;
                case "rmBranch":
                    rmBranch(args);
                    break;
                case "reflog":
                    reflog(args);
                    break;
                case "find":
                    find(args);
                    break;
                case "help":
                    help(args);
                    break;


                default:
                    System.err.println("No command with that name exists.");
                    break;
            }
        }
    }

    /**
     * 判断用户参数输入是否正确
     */
    public static void checkArgsValid(String[] args, int argsLength) {
        if (args.length != argsLength) {
            System.err.println("Incorrect operands. See git help");
            System.exit(0);
        }
    }

    /**
     * 判断是否已经初始化仓库
     *
     * @return
     */
    public static boolean isInitialized() {
        return Files.exists(utility.getGitDirPath());
    }

    /**
     * init操作
     * 1.判断工作区中（当前路径）是否存在.Mygit目录，已存在则打印提示信息；
     * 2.创建.Mygit目录，在其中创建objects目录用于blob、tree等对象；objects文件夹里创建commits文件夹，保存commit文件快照，命名均为其hash值
     * 3.创建Index对象序列化到.git目录下用于储存 文件名-hash值的对应条目（初始为空）；
     * 4.创建HEAD文件储存目前被检出的分支末端的commit id（初始为空）。
     * 5.创建refs文件夹，保存各分支名字的文件，内容是分支末端的commit_id
     */
    public static void init(String[] args) {
        checkArgsValid(args, 1);
        try {
            Files.createDirectory(Paths.get(".Mygit"));//新建文件夹".Mygit"
            //Files.createDirectory(utility.getGitDirPath());//新建文件夹".Mygit"
            Files.createDirectory((utility.getFilesPath()));//新建objects文件夹
            Files.createDirectory(utility.getCommitsPath());//新建文件夹commits
            Files.createFile(utility.getHeadPath());//新建文件head
            Files.createFile(utility.getIndexFilePath());//新建文件index
            Files.createDirectory(utility.getRefsPath());//新建文件refs

            System.out.println("init 命令执行");

        } catch (FileAlreadyExistsException e) {
            System.err.println("A git.Mygit version-control system already exists in the current directory.");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * add操作:暂存指定文件
     * 1.读取args[1],如果是"."表示为暂存所有工作区文件，否则只暂存对应文件
     * （1）当暂存所有工作区文件，创建工作区所有blob对象（blob+" "+内容字符数+ 内容），对象名字为对应的hash值，序列化到objects文件夹下，清空index文件内容,，并存储文件名-对应blob的hash值入index
     * （2）当暂存对应文件，创建对应blob对象序列化到objects文件夹下，读取当前index文件内容，新增对应文件名-hash值 条目，并保存新的index文件
     * 2.当add的文件，不存在于工作区时，
     * （1）遍历index文件条目，判断是否存在同名文件或文件夹，如果存在，在暂存区中删除对应条目；
     * （2）如果不存在，输出异常
     */
    public static void add(String[] args) throws IOException {
        ObjectOutputStream oos = null;
        checkArgsValid(args, 2);
        String s = args[1];//获取文件名
        Index index_object = null;
        try {
            //读取index中内容（防止空指针）
            if (!utility.readFile(utility.getIndexFilePath().toString()).equals("")) {
                index_object = Index.deSerialFrom();
            }

            if (s.equals(".")) {
                s = "";
                //输入add . 时，将只存在于暂存区中，而不存在于工作区的文件记录，从index中删除（用将index清空的方法，重写index）
                if (index_object != null) {
                    index_object = new Index(new TreeMap<>());
                    oos = new ObjectOutputStream(new FileOutputStream(utility.getIndexFilePath().toString()));
                    oos.writeObject(index_object);
                }
            }
            Path path = Paths.get(s);

            TreeMap<String, String> new_tracking = new TreeMap<>();
            //当add的文件，只存在于暂存区（index），而不存在于工作区时，在暂存区中删除对应条目；
            if (!Files.exists(path)) {
                boolean file_exists = false;
                TreeMap<String, String> tracking;
                if (index_object != null) {
                    tracking = index_object.getTracking();
                } else throw new NotIndexedException();
                for (Map.Entry<String, String> stringStringEntry : tracking.entrySet()) {
                    Map.Entry entry = stringStringEntry;
                    String filePath = entry.getKey().toString();
                    if (filePath.startsWith(path + File.separator) || filePath.equals(path.toString())) {
                        file_exists = true;
                    } else new_tracking.put(filePath, tracking.get(filePath));
                }
                if (!file_exists) throw new NoSuchFileException();
                else System.out.println("已在暂存区删除" + args[1]);
                index_object = new Index(new_tracking);
                oos = new ObjectOutputStream(new FileOutputStream(utility.getIndexFilePath().toString()));
                oos.writeObject(index_object);
            }
            //创建对应blob对象序列化到objects文件夹下
            else {
                List<Path> files = Files.walk(path).filter((p) -> (!Files.isDirectory(p) && !(p.toString().charAt(0) == '.'))).collect(Collectors.toList());
                Blob.writeBlob(files);
                Index.writeIndex(files);
            }
        } catch (NoSuchFileException e) {
            System.err.println("No file with that name exists. ");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                oos.close();
            }
        }
    }


    /**
     * commit操作
     * 1.将index中所有条目生成tree对象序列化到objects文件夹下；
     * （1）建立tree文件 读取当前index的TreeMap条目，并存入tree文件对应格式的TreeMap，内容是 tree blob 文件名 hash值
     * 2.将commit对象序列化到objects文件夹下，commit对象包括以下属性：parent commit id、本次commit所生成tree id，message、时间戳；
     * （1）读取head文件中上一次的commit id，判断parent commit id和本次commit中的tree id是否一致，如果一致，则输出异常
     * (2) 如果index文件为空，输出异常
     * （3）创建commit-对象。如果该commit的parent commit_id为空（刚初始化），所属分支为master,否则该Commit对象所属分支为其parent 分支
     * 3.更新HEAD文件中的分支名称。
     * 4.在refs中保存当前branch的commit_id
     * 5.通过比较两次tree中储存的文件目录，打印本次commit相对上一次commit的文件变动情况（增加、删除、修改）
     */
    public static void commit(String[] args) {
        checkArgsValid(args, 3);
        try {
            String parent;
            if (!utility.readFile(utility.getHeadPath().toString()).equals("")) {
                parent = Commit.get_head_commit_id();//上一次的commit id
            } else parent = "";

            String message = args[2];
            ZonedDateTime timestamp = ZonedDateTime.now();//commit时间
            String author = System.getProperty("user.name");//作者
            Tree tree = Tree.addTree();//建立tree文件 内容是 tree blob 文件名 hash值
            Commit.addCommit(parent, message, tree, timestamp, author);//将commit对象序列化到objects文件夹下


        } catch (Exception e) {
            e.printStackTrace();

        }

    }


    /**
     * rm 操作
     * 1.判断文件是否存在于工作区，如果不存在，输出提醒信息
     * 2.判断文件名是否存在于暂存区，如果不存在，输出提醒信息
     * 3.git rm --cached 命令： 删除暂存区同名文件，但保留工作区的文件。
     * 4.git rm -f 命令：删除工作区和暂存区同名文件，要删除的文件和当前版本库文件的内容相同或不同都可以
     * 5.git rm 命令：当同名文件和当前版本库文件的内容相同时，删除工作区和暂存区同名文件，否则输出提醒信息
     * （1）当同名文件与暂存区同名文件内容相同时，提醒该文件新版本未commit到版本库，使用 --cached 或 --f命令删除文件
     * （2）当同名文件与暂存区同名文件内容不相同时，提醒该文件新版本未更新到暂存区，使用 --cached  或--f 命令删除文件
     */

    public static void rm(String[] args) throws Exception {
        ObjectOutputStream oos = null;
        Path path_file = null;
        String filename = null;
        String operands = null;
        TreeMap<String, String> map_file = null;
        try {
            Index index_object = Index.deSerialFrom();
            TreeMap<String, String> tracking;
            if (index_object != null) {
                tracking = index_object.getTracking();
            } else throw new NotIndexedException();
            File index_file = new File(utility.getIndexFilePath().toString());
            if (args.length == 3) {
                path_file = Paths.get(args[2]);
                filename = path_file.toString();
                operands = args[1];
            } else if (args.length == 2) {
                path_file = Paths.get(args[1]);
                filename = path_file.toString();
                operands = "";
            } else {
                System.err.println("Incorrect operands. See git help.");
                System.exit(0);
            }
            if (!Files.exists(path_file)) throw new NoSuchFileException();
            File file = new File(filename);
            if (file.isDirectory()) {
                map_file = utility.get_map(path_file);
                if (utility.is_map_changed_added(map_file, tracking)) throw new NotIndexedException();
            } else if (!tracking.containsKey(filename)) throw new NotIndexedException();
            if (operands.equals("--f") || operands.equals("--cached")) {
                //删除index中对应条目
                if (file.isDirectory()) {//如果是文件夹，删除与文件夹文件path相同的键值对
                    for (Map.Entry<String, String> stringStringEntry : map_file.entrySet()) {
                        Map.Entry entry = stringStringEntry;
                        String filePath = entry.getKey().toString();
                        tracking.remove(filePath);
                    }
                } else tracking.remove(filename);
                if (operands.equals("--f")) {//在工作区中删除该文件
                    utility.delete_allDir(path_file);
                }
                System.out.println(filename + " " + "deletes successfully");
            } else if (operands.equals("")) {//rm +filename
                TreeMap<String, String> last_commit = Tree.get_head_tree().getTracking();
                if (file.isDirectory()) {
                    if (!utility.is_map_changed_added(map_file, last_commit)) {
                        utility.delete_allDir(path_file);
                        for (Map.Entry<String, String> stringStringEntry : map_file.entrySet()) {
                            Map.Entry entry = stringStringEntry;
                            String filePath = entry.getKey().toString();
                            tracking.remove(filePath);
                        }
                        System.out.println("Directory " + filename + " deletes successfully");
                    } else if (!utility.is_map_changed_added(map_file, tracking)) {
                        System.out.println(filename + " has changes staged in the index");
                        System.out.println("use --cached to keep the file, or -f to force removal.");
                    } else {
                        System.out.println(filename + " has local modifications");//当同名文件与暂存区同名文件内容不相同时
                        System.out.println("use --cached to keep the file, or -f to force removal.");
                    }
                } else {
                    String blob_id = utility.str2Hash(utility.readFile(filename));
                    if (last_commit.get(filename).equals(blob_id)) {//当同名文件和当前版本库文件的内容相同时
                        tracking.remove(filename);
                        Files.delete(utility.getPath().resolve(filename));
                        System.out.println(filename + " deletes successfully");
                    } else if (tracking.get(filename).equals(blob_id)) {//当同名文件与暂存区同名文件内容相同时
                        System.out.println(filename + " has changes staged in the index");
                        System.out.println("use --cached to keep the file, or -f to force removal.");
                    } else {
                        System.out.println(filename + " has local modifications");//当同名文件与暂存区同名文件内容不相同时
                        System.out.println("use --cached to keep the file, or -f to force removal.");
                    }
                }
            } else {
                System.err.println("Incorrect operands. See git help");
                System.exit(0);
            }

            oos = new ObjectOutputStream(new FileOutputStream(index_file));
            oos.writeObject(index_object);
        } catch (NoSuchFileException e) {
            System.err.println("No file with this name exists .");
            System.exit(0);
        } catch (NotIndexedException e) {
            System.err.println("Not indexed yet.");
            System.exit(0);
        } finally {
            if (oos != null) {
                oos.close();
            }
        }
    }

    /**
     * 1.从HEAD文件中读取当前分支，若HEAD为空打印提示信息。
     * 2.在refs文件夹中找到当前分支的末端commit,打印parent commit id，author,message，commit时间等信息
     * 3.读出该commit中存放的前一次commit id，
     * 4.反复执行2、3直到打印完分支所有commit的内容。
     */
    public static void log(String[] args) {
        checkArgsValid(args, 1);

        try {
            String parent = Commit.get_head_commit_id();
            if (parent.equals("")) throw new NotCommittedException();
            while (!parent.equals("")) {
                Commit previous_commit_object = Commit.getCommit(parent);
                System.out.println("===");
                System.out.println("commit_id " + parent + " : " + previous_commit_object);
                System.out.println();
                if (previous_commit_object != null) {
                    parent = previous_commit_object.get_parent();
                }
            }

        } catch (NotCommittedException e) {
            System.err.println("No commit yet");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 1.取出args[] 中的commit id，以及reset模式，其中mixed是默认模式，
     * 2.判断objects文件夹中是否存在对应的commit对象，
     * 3.reset --soft：修改当前branch文件内容为给定commit id
     * 4.reset --mixed：在3的基础上，重置暂存区到对应commit，
     * 5.reset --hard：在4的基础上，重置工作区与暂存区内容一致。
     */
    public static void reset(String[] args) throws IOException {

        FileWriter fw = null;
        ObjectOutputStream oos = null;
        Index index_object;
        String commitStr = null;
        Commit commit;
        TreeMap<String, String> map;
        try {
            //获取当前最新提交对应的Tree，并把它的文件记录存储在map_tree中。
            TreeMap<String, String> map_tree = Tree.get_head_tree().getTracking();
            //遍历当前工作区文件，用它们的文件记录存储在map_workspace中。
            TreeMap<String, String> map_workspace = utility.get_map(utility.getPath());
            //如果工作目录存在未被commit跟踪，且将被覆写的文件，输出错误信息：
            if (utility.is_map_changed_added(map_workspace, map_tree) && args[1].equals("--hard")) {
                System.err.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }

            if (args.length == 2 || (args.length == 3 && (args[1].equals("--mixed") || args[1].equals("--hard") || args[1].equals("--soft")))) {
                if (args.length == 2) commitStr = args[1];
                if (args.length == 3) commitStr = args[2];
                //判断该commitStr是否存在，如果没有对应的commit存在，打印错误信息：
                if (Files.exists(utility.getCommitsPath().resolve(commitStr)))
                    commit = Commit.deSerialFrom(utility.getCommitsPath().resolve(commitStr).toString());
                else throw new NoSuchCommitException();
                //修改当前branch文件内容为给定commit id
                fw = new FileWriter(utility.getRefsPath().resolve(utility.get_branch()).toString());
                fw.write(commitStr);
                fw.close();

                if (args[1].equals("--mixed") || args[1].equals("--hard") || args.length == 2) {
                    //重置暂存区到对应commit
                    Tree tree = Tree.get_tree(commit);
                    map = tree.getTracking();
                    index_object = new Index(map);
                    File index_file = new File(utility.getIndexFilePath().toString());
                    oos = new ObjectOutputStream(new FileOutputStream(index_file));
                    oos.writeObject(index_object);
                    //重置工作区与暂存区内容一致
                    if (args[1].equals("--hard")) {
                        utility.deleteDir(Paths.get(""));
                        for (Map.Entry<String, String> stringStringEntry : index_object.getTracking().entrySet()) {
                            Map.Entry entry = stringStringEntry;
                            String filePath = entry.getKey().toString();
                            File file = new File(utility.getPath().resolve(filePath).toString());
                            if (file.isDirectory()) utility.mkdir(new File(filePath));
                            else {
                                utility.mkdir(file.getParentFile());
                                fw = new FileWriter(file);
                                Blob blob = Blob.deSerialFrom(utility.getFilesPath().resolve(entry.getValue().toString()).toString());
                                fw.write(blob.getContent());
                                fw.close();
                            }
                        }
                    }
                }
            } else {
                System.err.println("Incorrect operands. See git help");
                System.exit(0);
            }

        } catch (NoSuchCommitException e) {
            System.err.println("No such commit exists.");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fw != null) {
                fw.close();
            }
            if (oos != null) {
                oos.close();
            }
        }

    }

    /**
     * pull 操作：使用socket在从远程存储库中拉取更改，并将本地工作区重置
     * 1.启动客户端套接字，并将返回的套接字分配给“socket_client”变量。
     * 2.客户端向服务器发送消息“pull”告知服务器发送文件
     * 3.删除当前本地 git 版本库。
     * 4.服务器用ZipOutputStream将版本库压缩并传输，客户端接收服务器的版本库并解压
     * 5.本地根据存储库的上一次提交重置工作区
     *
     * @param args
     * @throws Exception
     */
    public static void pull(String[] args) throws Exception {
        checkArgsValid(args, 1);
        Socket socket_client = null;
        try {
            socket_client = utility.start_client();
            System.out.println("客户端执行pull操作");
            utility.sendMessage(socket_client, "pull");
            utility.receive(socket_client);
            String commitStr = Commit.get_head_commit_id();
            String[] list = {"git", "--hard", commitStr};
            reset(list);
        } finally {
            if (socket_client != null) {
                socket_client.close();
            }

        }
    }


    /**
     * push 操作：使用socket将更改推送到远程存储库，并将远程工作区重置
     * 1.启动客户端套接字，并将返回的套接字分配给“socket_client”变量。
     * 2.客户端向服务器发送消息“push”告知服务器接收文件
     * 3.删除远程存储哭的 git 版本库。
     * 4.客户端用ZipOutputStream将版本库压缩并传输，服务器接收客户端的版本库并解压
     * 5.远程根据存储库的上一次提交重置工作区
     *
     * @param args
     * @throws Exception
     */
    public static void push(String[] args) throws Exception {
        checkArgsValid(args, 1);
        Socket socket_client = null;
        try {
            socket_client = utility.start_client();
            System.out.println("客户端执行push操作");
            utility.sendMessage(socket_client, "push");
            utility.send(socket_client);
        } finally {
            if (socket_client != null) {
                socket_client.close();
            }
        }
    }


    /**
     * diff 操作
     * 将当前工作树与暂存区中的文件进行比较，并打印出差异。
     * 获取记录当前工作区文件状态的treemap和index 的treemap，对比键值判断是否文件内容是否修改，或新增删除文件
     *
     * @param args
     */
    public static void diff(String[] args) {
        checkArgsValid(args, 1);
        TreeMap<String, String> map_workspace, map_index = null;//使用 map_workspace 存储本地版本，使用 map_index 存储即将提交的版本。
        Index index_object = Index.deSerialFrom();
        if (index_object != null) {
            map_index = index_object.getTracking();
        }
        map_workspace = utility.get_map(utility.getPath());
        //使用两个嵌套循环来迭代“map_workspace”和“map_index”条目。
        //如果“map_workspace”中的条目与“map_index”中的条目具有相同的键并且它们的值不同，它会打印出一条消息，指示差异以及工作树和索引中相应文件的内容。
        if (map_workspace != null && map_index != null) {
            for (Map.Entry<String, String> entry1 : map_workspace.entrySet()) {
                for (Map.Entry<String, String> entry2 : map_index.entrySet()) {
                    if (entry1.getKey().equals(entry2.getKey())) {
                        if (!entry1.getValue().equals(entry2.getValue())) {
                            System.out.println("diff -- " + entry1.getKey() + " changes.");
                            System.out.println("workspace/" + entry1.getKey() + " : ");
                            System.out.println(utility.readFile(entry1.getKey()));//打印工作区差异文件
                            System.out.println("index/" + entry2.getKey() + " : ");
                            System.out.println(Blob.deSerialFrom(utility.getFilesPath().resolve(entry2.getValue()).toString()).getContent());//打印暂存区差异文件
                        }
                    }
                }
                //如果“map_workspace”中的条目在“map_index”中没有相应的键，它会打印一条消息，指示该文件已添加。
                if (!map_index.containsKey(entry1.getKey()))
                    System.out.println("diff -- " + entry1.getKey() + " adds.");
            }
        }
        //使用另一个嵌套循环遍历“map_index”条目，如果“map_index”中的条目在“map_workspace”中没有对应的键，它会打印出一条消息，指示该文件已被删除
        if (map_index != null) {
            for (Map.Entry<String, String> entry2 : map_index.entrySet()) {
                if (!map_workspace.containsKey(entry2.getKey()))
                    System.out.println("diff -- " + entry2.getKey() + " deletes.");
            }
        }
    }

    /**
     * 新增一个分支，并让这个分支指向head所指向的commit
     * 1.检查代表输入参数指定的分支的目录路径是否已经存在，如果是，它会抛出“AlreadyExistBranchException”
     * 2.在 refs 目录中创建一个新文件，代表新分支，名称由输入参数指定，并将当前提交commit的哈希值写入其中。
     * 3.使用 FileWriter 对象用新的分支名称更新头文件
     *
     * @param args 命令行参数
     */
    public static void branch(String[] args) {
        checkArgsValid(args, 2);
        FileWriter fw = null;
        ObjectOutputStream oos;
        try {
            if (Files.exists(utility.getRefsPath().resolve(args[1]))) throw new AlreadyExistBranchException();
            //在refs文件夹中存储新的分支名称为文件名的文件，写入CommitStr
            fw = new FileWriter(utility.getRefsPath().resolve(args[1]).toString());
            fw.write(Commit.get_head_commit_id());
            fw.close();
            fw = new FileWriter(utility.getHeadPath().toString());
            fw.write(args[1]);
            fw.close();
        } catch (AlreadyExistBranchException e) {
            System.err.println("A branch with that name already exists.");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * branch 操作
     * 将当前分支更改为输入参数中指定的分支,允许用户将当前分支更改为另一个分支，以便在项目的不同版本之间切换。
     * 1.检查代表输入参数指定的分支的目录路径是否存在，如果不存在，则抛出“NoSuchBranchException”
     * 2.检查工作目录是否有任何未跟踪的更改，如果有，输出错误信息
     * 3.检查当前分支是否与输入参数相同，如果是则抛出错误消息并退出
     * 4.使用 FileWriter 对象将头文件更新为新的分支名称
     * 5.使用 reset(list) 方法更新到新指定分支的最新提交
     * 6.打印一条消息，表明分支已被切换。
     *
     * @param args 重新指向的分支名称
     * @throws IOException
     */
    public static void checkout(String[] args) throws IOException {
        checkArgsValid(args, 2);
        FileWriter fw;
        try {
            if (!Files.exists(utility.getRefsPath().resolve(args[1]))) throw new NoSuchBranchException();
            //如果工作目录存在未被commit跟踪，且将被覆写的文件，输出错误信息:
            //获取当前最新提交对应的Tree，并把它的文件记录存储在map_tree中。
            TreeMap<String, String> map_tree = Tree.get_head_tree().getTracking();
            //遍历当前工作区文件，用它们的文件记录存储在map_workspace中。
            TreeMap<String, String> map_workspace = utility.get_map(utility.getPath());
            if (utility.is_map_changed_added(map_workspace, map_tree)) {
                System.err.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
            //如果当前branch为checkout branch，输出错误信息
            if (utility.get_branch().equals(args[1])) {
                System.err.println("No need to checkout the current branch.");
                System.exit(0);
            }
            //更新至branch 末端commit状态,注意先更新工作区再更新head文件
            String commitStr = utility.readFile(utility.getRefsPath().resolve(args[1]).toString());
            String[] list = {"git", "--hard", commitStr};
            reset(list);
            //更新head文件的branch名称
            fw = new FileWriter(utility.getHeadPath().toString());
            fw.write(args[1]);
            fw.close();

            System.out.println("Switched to branch " + args[1]);
        } catch (NoSuchBranchException e) {
            System.err.println("No such branch exists.");
            System.exit(0);
        }
    }


    /**
     * status 操作
     * 打印状态，分为三种：
     * 1. 跟踪中的文件
     * 2. 已经暂存但是在工作区已经被修改或者删除的文件
     * 3. 工作目录中没有被跟踪的文件
     *
     * @param args 命令行参数
     */

    public static void status(String[] args) {
        checkArgsValid(args, 1);
        TreeMap<String, String> map_workspace, map_index;
        Index index_object = Index.deSerialFrom();
        map_index = index_object.getTracking();//缓存区
        map_workspace = utility.get_map(utility.getPath());//工作区
        System.out.println(utility.getPath());
        System.out.println(map_workspace);
        //HashSet保存文件名
        HashSet<String> untrackedFiles = new HashSet<>(), modifiedFiles = new HashSet<>(),
                deletedFiles = new HashSet<>(), trackingFiles = new HashSet<>();
        //读取当前所有的分支名字
        System.out.println();
        System.out.println("==branch==");

        File f = new File(utility.getRefsPath().toString());
        File[] files = f.listFiles();
        if (files.length == 0) System.out.println("current working branch: * master");
        else {
            for (File fi : files) {
                if (!fi.isHidden()) {
                    if (fi.getName().equals(utility.get_branch())) {
                        System.out.println("current working branch: * " + fi.getName());
                    } else System.out.println(fi.getName());
                }
            }
        }
        //对比工作区与缓存区内容
        for (Map.Entry<String, String> entry1 : map_workspace.entrySet()) {
            for (Map.Entry<String, String> entry2 : map_index.entrySet()) {
                trackingFiles.add(entry2.getKey());//储存当前缓存区文件名
                if (entry1.getKey().equals(entry2.getKey())) {
                    if (!entry1.getValue().equals(entry2.getValue()))
                        //如果工作区与缓存区文件名相同，但内容不同，说明文件修改
                        modifiedFiles.add(entry1.getKey());
                }
            }
            //如果工作区的文件名不存在于缓存区，说明该文件在工作区新增
            if (!map_index.containsKey(entry1.getKey()))
                untrackedFiles.add(entry1.getKey());
        }
        //如果缓存区的文件名不存在于工作区，说明该文件在工作区已删除
        for (Map.Entry<String, String> entry2 : map_index.entrySet()) {
            if (!map_workspace.containsKey(entry2.getKey()))
                deletedFiles.add(entry2.getKey());
        }


        System.out.println();
        System.out.println("tracking files:");
        trackingFiles.forEach(System.out::println);

        System.out.println();
        System.out.println("Staged but modified files:");
        modifiedFiles.forEach(System.out::println);


        System.out.println();
        System.out.println("Staged but removed files:");
        deletedFiles.forEach(System.out::println);


        System.out.println();
        System.out.println("Untracked files:");
        untrackedFiles.forEach(System.out::println);
        System.out.println();


    }

    /**
     * rmBranch 操作：删除指定分支
     * 1.检查被删除的分支是否为当前分支，如果是则抛出异常并退出程序。
     * 2.如果分支不存在，抛出异常并退出程序。
     * 3.如果该分支存在，删除 refs 目录中代表该分支的文件。
     *
     * @param args 需要删除的分支名称
     */
    public static void rmBranch(String[] args) {
        checkArgsValid(args, 2);
        try {
            if (utility.get_branch().equals(args[1])) throw new DeleteCurrentBranchException();
            else if (!Files.exists(utility.getRefsPath().resolve(args[1]))) throw new NoSuchBranchException();
            else Files.delete(utility.getRefsPath().resolve(args[1]));
        } catch (DeleteCurrentBranchException e) {
            System.err.println("Can not remove the current branch.");
            System.exit(0);
        } catch (NoSuchBranchException e) {
            System.err.println("A branch with that name does not exist.");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * reflog操作：打印本Repo中所有的提交记录
     * 1.调用方法 get_commits() 创建存储库中所有提交的 HashSet。
     * 2.打印其中的commit对象
     *
     * @param args 命令行参数
     */
    public static void reflog(String[] args) {
        checkArgsValid(args, 1);

        HashSet<String> commits = utility.get_commits();

        for (String commitStr : commits) {
            System.out.println("===");
            System.out.println("commit_id " + commitStr + " : " + Commit.getCommit(commitStr));
            System.out.println();
        }
    }

    /**
     * 在存储库中搜索具有特定消息的提交
     * 1.将变量 noSuchCommit 初始化为 true，并调用方法 get_commits() 创建存储库中所有提交的 HashSet。
     * 2.遍历提交 HashSet 中的每个提交，如果提交的消息与作为参数传入的消息匹配，它会打印提交对象并将 noSuchCommit 设置为 false。
     * 3.在遍历所有提交之后，检查 noSuchCommit 的值，如果它仍然为真，则意味着没有找到具有给定消息的提交,输出错误信息。
     *
     * @param args 要搜索的message信息
     */
    public static void find(String[] args) {
        checkArgsValid(args, 2);

        boolean noSuchCommit = true;
        HashSet<String> commits = utility.get_commits();

        for (String commitStr : commits) {

            if (Commit.getCommit(commitStr).getMessage().equals(args[1])) {
                System.out.println(Commit.getCommit(commitStr));
                System.out.println("===");
                noSuchCommit = false;
            }

        }
        if (noSuchCommit) System.out.println("Found no commit with that message.");

    }

    public static void help(String[] args) {
        checkArgsValid(args, 1);
        System.out.println("git init -- 在当前目录初始化.Mygit仓库");
        System.out.println("git add [filename]/ .-- 跟踪或者暂存最新版文件或文件夹");
        System.out.println("git commit --m [message] -- 提交暂存区");
        System.out.println("git rm (--cached/--f) [filename] -- 将指定文件或文件夹从暂存区删除，同时也在磁盘上删除该文件");
        System.out.println("git log  -- 按照时间逆序打印当前分支的所有历史提交记录，直到第一次提交");
        System.out.println("git reset (--soft/mixed/hard) [commit_id] --检出到指定提交");
        System.out.println("git pull/push --下载/上传远程代码");
        System.out.println("git diff --将当前工作树与暂存区中的文件进行比较，并打印出差异");
        System.out.println("git branch [branch name] --添加分支");
        System.out.println("git checkout [branch name] --检出到指定分支");
        System.out.println("git status --打印当前状态");
        System.out.println("git rmBranch [branch name] --删除指定分支");
        System.out.println("git reflog --打印出本仓库的所有提交记录");
        System.out.println("git find [commit message] --打印出所有message为给定message的全部提交记录");

    }
}











