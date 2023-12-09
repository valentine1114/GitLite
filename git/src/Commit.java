import Exceptions.NoSuchCommitException;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TreeMap;

public class Commit implements Serializable {

    private static final long serialVersionUID = -4761380587287048893L;
    private final String type;//对象类型
    private final String parent;//parent commit的commitStr

    private final String treeHash;//储存本次commit的文件目录
    private final String author;//commit的作者
    private final ZonedDateTime timestamp;//时间戳信息
    private final String message;//提交信息


    public Commit(String parent, String treeHash, String author, ZonedDateTime timestamp, String message) {//构造函数
        this.type = "commit";
        this.parent = parent;
        this.treeHash = "tree" + " " + treeHash;
        this.author = author;
        this.timestamp = timestamp;
        this.message = message;

    }

    /**
     * 2.将commit对象序列化到objects文件夹下，commit对象包括以下属性：parent commit id、本次commit所生成tree id，message、时间戳；
     * （1）读取head文件中上一次的commit id，判断parent commit id和本次commit中的tree id是否一致，如果一致，则输出异常
     * (2) 如果index文件为空，输出异常
     * （3）创建commit-对象。如果该commit的parent commit_id为空（刚初始化），所属分支为master,否则该Commit对象所属分支为其parent 分支
     * 3.更新HEAD文件中的分支名。
     * 4.在refs中保存当前branch的commit_id
     * 5.通过比较两次tree中储存的文件目录，打印本次commit相对上一次commit的文件变动情况（增加、删除、修改）
     */

    public static void addCommit(String parent, String message, Tree tree, ZonedDateTime timestamp, String author) throws Exception {
        FileWriter fw = null;
        ObjectOutputStream oos = null;
        Commit commit_object;

        try {
            //判断是否需要commit
            if (!parent.equals("")) {
                Commit previous_commit_object = getCommit(parent);
                if (("tree " + utility.str2Hash(tree.toString())).equals(previous_commit_object.getTreeHash())) //如果这次提交的文件和上次完全一样，就不用提交了（判断两次树的hash值是否相同）)
                {
                    System.err.println("No changes added to the commit.");
                    System.exit(0);
                }
            }

            //创建commit-对象。
            commit_object = new Commit(parent, utility.str2Hash(tree.toString()), author, timestamp, message);

            //在head中输入当前所属分支名称
            //如果该commit的parent commit_id为空（刚初始化），所属分支为master,否则该Commit对象所属分支为其parent 分支，即head值不变动，更新refs的分支文件内容。

            if (parent.equals("")) {
                fw = new FileWriter(utility.getHeadPath().toString());
                fw.write("master");
                fw.close();
            }

            //在refs中保存当前branch的commitStr
            fw = new FileWriter(utility.getRefsPath().resolve(utility.get_branch()).toString());
            fw.write(getCommitStr(commit_object));
            fw.close();

            //储存commit对象
            String SHA1 = utility.str2Hash(commit_object.toString());
            File Commit_file = new File(utility.getCommitsPath().resolve(SHA1).toString());
            oos = new ObjectOutputStream(new FileOutputStream(Commit_file));
            oos.writeObject(commit_object);

            //打印本次commit与上次的文件变动情况，通过两次tree中储存的文件目录比较
            if (!parent.equals("")) {
                Commit previous_commit = deSerialFrom(utility.getCommitsPath().resolve(parent).toString());
                Tree previous_tree = Tree.get_tree(previous_commit);
                TreeMap<String, String> p_map, n_map;
                p_map = previous_tree.getTracking();
                n_map = tree.getTracking();

                int change = 0;
                int delete = 0;
                int add = 0;
                for (Map.Entry<String, String> entry1 : n_map.entrySet()) {
                    for (Map.Entry<String, String> entry2 : p_map.entrySet()) {
                        if (entry1.getKey().equals(entry2.getKey())) {
                            if (!entry1.getValue().equals(entry2.getValue()))
                                change = change + 1; //如果工作区与缓存区文件名相同，但内容不同，说明文件修改
                        }
                    }
                    if (!p_map.containsKey(entry1.getKey())) add = add + 1;//如果工作区的文件名不存在于缓存区，说明该文件在工作区新增
                }
                for (Map.Entry<String, String> entry2 : p_map.entrySet()) {
                    if (!n_map.containsKey(entry2.getKey())) delete = delete + 1;//如果缓存区的文件名不存在于工作区，说明该文件在工作区已删除
                }
                System.out.println(change + " file changed, " + add + " insertions(+), " + delete + " deletions(-)");
            } else System.out.println(tree.getTracking().size() + " insertions(+).");

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
     * 用于从位于指定路径的文件中反序列化 commit对象。
     * 该方法使用 ObjectInputStream 从文件中读取序列化数据，然后调用 readObject() 方法将其反序列化为 commit对象。
     * 该方法同时捕获 IOException 和 ClassNotFoundException 并通过打印堆栈跟踪并在出现任何异常时返回 null 来处理它们。
     *
     * @param path commit对象文件所在路径
     * @return commit对象
     */
    public static Commit deSerialFrom(String path) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
            return (Commit) in.readObject();
        } catch (IOException e) {
            System.err.println("Not committed yet.");
            System.exit(0);
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 返回以字符串格式传递的commit的哈希值
     *
     * @param commit commit对象
     * @return 以字符串格式传递的提交对象的哈希值
     */
    public static String getCommitStr(Commit commit) {
        return utility.str2Hash(commit.toString());
    }


    /**
     * 通过commit的哈希值 查找具有该哈希值名称的文件并反序列化该文件中的对象。
     *
     * @param commit_id commit对象的哈希值
     * @return commit对象
     */
    public static Commit getCommit(String commit_id) {
        Commit temp = null;
        try {
            temp = deSerialFrom(utility.getCommitsPath().resolve(commit_id).toString());
            if (temp == null)
                throw new NoSuchCommitException();
        } catch (NoSuchCommitException e) {
            System.err.println("No such commit exists.");
            System.exit(0);
        }
        return temp;
    }


    /**
     * 返回当前分支的最末端commit的哈希值
     */
    public static String get_head_commit_id() {
        return utility.readFile(utility.getRefsPath().resolve(utility.get_branch()).toString());
    }

    /**
     * 返回当前分支的最末端commit对象
     */
    public static Commit get_head_commit() {
        return getCommit(get_head_commit_id());
    }


    /**
     * 返回该次提交的的tree 对象的哈希值
     */
    public String getTreeHash() {
        return treeHash;
    }

    /**
     * 返回该次提交的parent commit哈希值
     */
    public String get_parent() {
        return parent;
    }

    /**
     * @return commit对象的字符串表示形式
     */
    public String toString() {
        return parent + " " + type + " " + treeHash + " " + author + " " + timestamp + " " + message;
    }

    /**
     * @return 返回commit对象的提交信息
     */
    public String getMessage() {
        return message;
    }


}




