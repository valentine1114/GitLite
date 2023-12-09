import Exceptions.NotIndexedException;

import java.io.*;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Tree implements Serializable {
    private static final long serialVersionUID = 1005101888404186969L;
    private static int size; // tree对象的字符数
    private final String type; //对象的类型
    private final TreeMap<String, String> tracking; //当前工作区快照构成的treemap

    public Tree(TreeMap<String, String> tracking) {//tree 的构造函数
        this.type = "tree";
        this.tracking = tracking;
    }

    /**
     * 该方法创建一个新的 Tree 对象并将其写入文件。
     * Tree 对象的创建方法是首先创建一个名为 tracking 的 TreeMap，然后使用从文件中反序列化的 Index 对象中的数据填充它。
     * 如果 Index 对象的跟踪数据为空，则抛出 NotIndexedException。 然后使用 ObjectOutputStream 将树对象写入文件， 最后返回树对象。
     *
     * @return 当前工作区快照构成的树对象
     * @throws Exception
     */
    public static Tree addTree() throws Exception {
        ObjectOutputStream oos = null;
        try {
            TreeMap<String, String> tracking = new TreeMap<>();
            Index index_object = Index.deSerialFrom();
            if (index_object.getTracking().size() == 0) throw new NotIndexedException();
            for (Map.Entry<String, String> stringStringEntry : index_object.getTracking().entrySet()) {
                Map.Entry entry = stringStringEntry;
                String key = "blob " + entry.getKey();
                String value = entry.getValue() + "";
                tracking.put(key, value);
                size += key.length() + value.length();
            }
            //创建tree对象
            Tree tree_object = new Tree(tracking);
            File tree_file = new File(utility.getFilesPath().resolve(utility.str2Hash(tree_object.toString())).toString());
            oos = new ObjectOutputStream(new FileOutputStream(tree_file));
            oos.writeObject(tree_object);
            return tree_object;

        } catch (NotIndexedException e) {
            System.err.println("Not Indexed yet.");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                oos.close();
            }
        }
        return null;
    }

    /**
     * 用于从位于指定路径的文件中反序列化 Tree 对象。
     * 该方法使用 ObjectInputStream 从文件中读取序列化数据，然后调用 readObject() 方法将其反序列化为 Tree 对象。
     * 该方法同时捕获 IOException 和 ClassNotFoundException 并通过打印堆栈跟踪并在出现任何异常时返回 null 来处理它们。
     *
     * @param path tree对象文件所在路径
     * @return tree对象
     */
    //反序列化
    public static Tree deSerialFrom(String path) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
            return (Tree) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 返回当前分支的最末端commit的tree对象
     */
    public static Tree get_head_tree() {
        return get_tree(Commit.get_head_commit());
    }

    /**
     * 通过commit 对象 查找该次commit提交的tree对象
     *
     * @param commit commit对象
     * @return 该次提交的tree对象
     */
    public static Tree get_tree(Commit commit) {
        Tree temp = Tree.deSerialFrom(utility.getFilesPath().resolve(Paths.get(commit.getTreeHash().substring(5))).toString());
        if (temp == null) System.out.println("No such tree exits");
        return temp;
    }

    /**
     * 该方法返回一个<String, String>类型的TreeMap，
     * 创建一个新的 TreeMap 对象，遍历原始 TreeMap 的条目，对于每个条目，它提取键和值，删除"blob "，将新条目放入带有 key 的新 TreeMap 中
     *
     * @return TreeMap跟踪的index副本。
     */
    //获取当前tree 将文件名映射到文件的哈希值的TreeMap
    public TreeMap<String, String> getTracking() {
        Iterator iter = tracking.entrySet().iterator();
        TreeMap<String, String> map = new TreeMap<>();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = entry.getKey().toString().substring(5);
            String value = entry.getValue().toString();
            map.put(key, value);
        }
        return map;

    }

    /**
     * 连接了字符串“type”变量、“size”变量的字符串表示和“tracking”变量的字符串表示，“tracking”变量是一个 TreeMap 对象，用空格分隔。
     *
     * @return tree对象的字符串表示形式
     */
    public String toString() {
        return type + " " + size + " " + tracking;
    }

}
