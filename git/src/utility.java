import Exceptions.NotCommittedException;

import java.io.*;
import java.math.BigInteger;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class utility {
    /**
     * 分别记录了Mygit文件夹，index目录，commit目录等文件名称
     * 主要供初始化Mygit对象时使用，后面一般从对应的对象的getPath()方法中获得路径
     */


//    public static Path getPath() {
//        return Paths.get("");
//    }//获取git目录所在路径
    public static Path getPath() {
        //Path currentDir = Paths.get("."); // get current working directory
        String currentDir = System.getProperty("user.dir");
        String gitDir = searchForGitDir(currentDir); // search for .git directory
        if (gitDir != null) {
            //System.out.println("Git directory found at: " + gitDir);
            return Paths.get(gitDir).getParent();

            // pass gitDir to subsequent methods
        } else {
            System.out.println("Git directory not found.");
            return null;
        }
        //return Paths.get("");
    }
    public static String searchForGitDir(String currentDir) {
        File file = new File(currentDir.toString());
        while (file != null) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory() && f.getName().equals(".Mygit")) {
                        return f.getAbsolutePath(); // return path of .git directory
                    }
                }
            }
            file = file.getParentFile(); // move to parent directory
        }
        return null; // .git directory not found
    }


    public static Path getGitDirPath() {
        return getPath().resolve(".Mygit");
    }//获取Mygit文件夹所在路径

    public static Path getIndexFilePath() {
        return getGitDirPath().resolve("index");
    }//获取index文件夹所在路径

    public static Path getFilesPath() {
        return getGitDirPath().resolve("objects");
    }//获取objects文件夹所在路径

    public static Path getCommitsPath() {
        return getFilesPath().resolve("commits");
    }//获取commits文件夹所在路径

    public static Path getHeadPath() {
        return getGitDirPath().resolve("HEAD");
    }//获取HEAD文件夹所在路径

    public static Path getRefsPath() {
        return getGitDirPath().resolve("refs");
    }//获取refs文件夹所在路径


    /**
     * 计算字符串的sha-1值
     *
     * @param str 字符串str
     * @return sha-1值
     */

    public static String str2Hash(String str) {//将字符串转换为hash值
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(str.getBytes());
            return new BigInteger(1, bytes).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "impossible";
        }
    }

    /**
     * 用于删除文件夹下所有非隐藏文件的方法
     * 调用过滤器方法以从流中排除目录和隐藏文件。 参数传递给 filter 的 lambda 表达式指定了过滤流的标准。 对于那些不是目录和隐藏文件的 Path 对象，它返回 true。
     * walk 方法返回一个 Stream<Path> 对象，它表示可以遍历的 Path 对象流。
     * 最后在stream上调用collect方法将过滤后的Path对象收集到一个List中。
     *
     * @param path 需要删除的文件夹路径
     */
    public static void deleteDir(Path path) {
        try {
            List<Path> files = Files.walk(path).filter((p) -> (!Files.isDirectory(p) && !(p.toString().charAt(0) == '.'))).collect(Collectors.toList());
            for (Path file : files) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用于递归删除文件夹及其内容的方法
     *
     * @param path 需要删除的文件夹路径
     * @throws IOException
     */
    public static void delete_allDir(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach(file -> {
                try {
                    if (Files.isDirectory(file)) {
                        // 如果是文件夹，递归调用 deleteFolder
                        delete_allDir(file);
                    } else {
                        // 否则，直接删除文件
                        Files.delete(file);
                    }
                } catch (IOException e) {
                    System.err.println("删除文件不存在");
                    System.exit(0);
                }
            });

            // 删除文件夹本身
            Files.delete(path);
        } else Files.delete(path);
    }


    /**
     * 用于读取文件内容的方法
     *
     * @param filePath 要读取的文件的路径
     * @return 文件内容
     */

    public static String readFile(String filePath) {

        int readLen;
        char[] buf = new char[1024];
        String content = "";
        try {
            FileReader fileReader = new FileReader(filePath);
            while ((readLen = fileReader.read(buf)) != -1) {
                content += new String(buf, 0, readLen);
            }
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回当前分支的名称
     */
    public static String get_branch() {
        try {
            String branch = utility.readFile(utility.getHeadPath().toString());
            if (branch == "") throw new NotCommittedException();
            else return branch;
        } catch (NotCommittedException e) {
            System.err.println("Not committed yet.");
            System.exit(0);
            return null;
        }
    }

    /**
     * 解压文件
     *
     * @param zipFilePath  解压的zip文件所在的路径
     * @param desDirectory 解压后文件的存储目录
     * @throws Exception
     */
    public static void unzip(String zipFilePath, String desDirectory) throws Exception {
        //检查存储目录是否存在，如果不存在就创建该目录。如果目录创建失败就抛出一个异常。
        File desDir = new File(desDirectory);
        if (!desDir.exists()) {
            boolean mkdirSuccess = desDir.mkdir();
            if (!mkdirSuccess) {
                throw new Exception("创建解压目标文件夹失败");
            }
        }
        // 创建一个 ZipInputStream 对象，用于读取 zip 文件的内容
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
        // 遍历 zip 文件中的每一个文件或目录
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        //对于每一个条目，它首先根据文件名和存储目录构建出文件的解压路径。
        // 然后判断这个条目是文件还是目录，如果是目录，就直接调用之前定义的mkdir()方法来创建，如果是文件，就先创建父目录，然后创建文件并写入文件内容。
        while (zipEntry != null) {
            String unzipFilePath = desDirectory + File.separator + zipEntry.getName();
            if (zipEntry.isDirectory()) { // 文件夹
                // 直接创建
                mkdir(new File(unzipFilePath));
            } else { // 文件
                File file = new File(unzipFilePath);
                // 创建父目录
                mkdir(file.getParentFile());
                // 写出文件流
                BufferedOutputStream bufferedOutputStream =
                        new BufferedOutputStream(new FileOutputStream(unzipFilePath));
                byte[] bytes = new byte[1024];
                int readLen;
                while ((readLen = zipInputStream.read(bytes)) != -1) {
                    bufferedOutputStream.write(bytes, 0, readLen);
                }
                bufferedOutputStream.close();
            }
            zipInputStream.closeEntry();
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();//关闭 ZipInputStream 和读取的文件流，完成解压。
    }

    /**
     * 用来创建文件夹的方法
     *
     * @param file 要创建的文件夹
     */
    public static void mkdir(File file) {
        if (null == file || file.exists()) {
            return;
        }
        mkdir(file.getParentFile());
        file.mkdir();
    }


    /**
     * 压缩文件或文件夹（包括所有子目录文件）
     *
     * @param sourceFile 要压缩的文件或者文件夹
     */
    public static void zipFileTree(File sourceFile) {
        ZipOutputStream zos = null;
        try {
            String zipFileName;
            //如果压缩的是文件夹，就把压缩后文件的名字设置成文件夹的名字加上 .zip 后缀。如果是单个文件，就把文件的名字改成文件名加上 .zip 后缀。
            if (sourceFile.isDirectory()) { // 目录
                zipFileName = sourceFile.getName() + ".zip";
            } else { // 单个文件
                zipFileName = sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf(".")) + ".zip";
            }
            // 创建一个 ZipOutputStream 对象来写入压缩文件，并调用递归函数 zip() 来遍历压缩文件夹中的文件，并将文件写入压缩文件中。
            zos = new ZipOutputStream(new FileOutputStream(zipFileName));
            zip(sourceFile, zos, "");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zos != null) {// 关闭流
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 递归压缩文件
     *
     * @param file         当前文件
     * @param zos          压缩输出流
     * @param relativePath 相对路径
     * @throws IOException IO异常
     */
    public static void zip(File file, ZipOutputStream zos, String relativePath) throws IOException {

        FileInputStream fis = null;
        try {
            if (file.isDirectory()) { // 当前为文件夹
                // 当前文件夹下的所有文件
                File[] list = file.listFiles();
                if (list != null) {
                    // 计算当前的相对路径
                    if (relativePath.length() == 0) relativePath += file.getName();
                    else relativePath += File.separator + file.getName();
                    // 递归压缩每个文件
                    for (File f : list) {
                        zip(f, zos, relativePath);
                    }
                }
            } else { // 压缩文件
                // 计算文件的相对路径
                if (relativePath.length() == 0) relativePath += file.getName();
                else relativePath += File.separator + file.getName();
                // 写入单个文件
                zos.putNextEntry(new ZipEntry(relativePath));
                fis = new FileInputStream(file);
                int readLen;
                byte[] buffer = new byte[1024];
                while ((readLen = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, readLen);
                }
                zos.closeEntry();
            }
        } finally {
            if (fis != null) {// 关闭流
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 用于接收文件
     *
     * @param socket 接收文件的socket
     */

    public static void receive(Socket socket) {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        String path = System.getProperty("user.dir");
        try {
            dis = new DataInputStream(socket.getInputStream());
            //通过输入流从客户端读取文件名和文件长度，如果文件名为 "文件不存在" 则表示客户端没有要传输的文件，直接结束程序。
            String fileName = dis.readUTF();//文件名字
            long fileLen = 0;
            //判断文件输入流是否有内容
            if (fileName.equals("文件不存在")) {
                System.out.println("文件不存在！");
                System.exit(0);
            } else fileLen = dis.readLong();//文件长度
            //接着根据文件名和目录地址构造出文件的存储路径。
            File file = new File(path + File.separatorChar + fileName);//文件传输地址
            byte[] buffer = new byte[1024];
            int len;
            System.out.println("开始接收");
            if (new File(utility.getGitDirPath().toString()).exists()) utility.delete_allDir(utility.getGitDirPath());
            //使用缓冲区来循环读取客户端传输过来的文件数据，并通过输出流将文件数据写入本地文件中。
            dos = new DataOutputStream(new FileOutputStream(file));
            while ((len = dis.read(buffer, 0, buffer.length)) != -1) {
                dos.write(buffer, 0, len);
                dos.flush();
                //只要服务器不把输出流关闭，客户端一直在循环读入，无法发送确认收到的信息
                // 传输文件时把文件大小一起进行传输，客户端自行判断当前收到的文件是否接收成功
                //比对文件的大小和服务端传过来的文件长度是否相同，如果相同就表示文件接收完成。
                if (file.length() == fileLen) {
                    System.out.println("接收完成");
                    //调用unzip() 方法解压文件, 解压完成后删除zip文件
                    unzip(Paths.get(path).resolve(fileName).toString(), path);
                    Files.deleteIfExists(utility.getPath().resolve(fileName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) dis.close();
                if (dos != null) dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 用于发送文件
     *
     * @param socket 发送文件的socket
     */

    public static void send(Socket socket) {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        String path = utility.getGitDirPath().toString();
        try {
            //通过zipFileTree() 方法将文件夹压缩成 .zip 文件,然后通过输出流向客户端发送文件。
            dos = new DataOutputStream(socket.getOutputStream());
            zipFileTree(new File(path));
            String zip_name = new File(path).getName() + ".zip";
            Path path_zip = Paths.get(zip_name);
            File file = new File(path_zip.toString());
            //通过 DataInputStream 读取要发送的文件并判断文件是否存在，如果文件不存在就发送"文件不存在"给客户端并结束程序。
            if (!file.exists()) {
                System.out.println("文件不存在！");
                dos.writeUTF("文件不存在");
                System.exit(0);
            } else System.out.println("开始传输");
            dis = new DataInputStream(new FileInputStream(file));
            //发送文件名和文件长度，之后再使用缓冲区循环读取本地文件并发送至客户端。
            dos.writeUTF(file.getName());
            dos.flush();
            dos.writeLong(file.length());
            dos.flush();
            //文件输出
            byte[] buffer = new byte[1024];
            int len;
            while ((len = dis.read(buffer, 0, buffer.length)) != -1) {
                dos.write(buffer, 0, len);
                dos.flush();
            }
            Files.deleteIfExists(path_zip);//发送完成后删除压缩包
            System.out.println("传输完成");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) dis.close();
                if (dos != null) dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 用给定的 socket 发送一条字符串消息
     *
     * @param socket 发送信息的socket
     * @param str    发送信息
     */

    public static void sendMessage(Socket socket, String str) {
        DataOutputStream dos;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(str);
            dos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从给定的 socket 上读取一条字符串消息
     *
     * @param socket 接收信息的socket
     * @return 接收的信息
     * @throws Exception
     */
    public static String getMessage(Socket socket) throws Exception {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        return dis.readUTF();
    }

    /**
     * 创建一个客户端
     *
     * @return 已经连接的socket对象
     */
    public static Socket start_client() {
        Socket socket_client = new Socket();
        try {
            Scanner in = new Scanner(System.in);
            System.out.print("请输入客户端端口号：");
            int port = in.nextInt();
            socket_client = new Socket(InetAddress.getByName("127.0.0.1"), port);
            System.out.println("当前客户端的IP：" + socket_client.getLocalAddress());
            System.out.println("当前客户端的端口号：" + socket_client.getLocalPort());
            System.out.println("已发起服务器连接");

        } catch (ConnectException e) {
            System.out.println("请先启动服务器");
            System.exit(0);
        } catch (BindException e) {
            System.out.println("端口号已被使用，请重新输入");
            start_client();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return socket_client;
    }

    /**
     * 从路径获取所有文件的文件名、文件内容并使用一个哈希函数计算文件内容的哈希值，最后将文件名和哈希值存入一个TreeMap中。
     *
     * @return 当前路径文件状态
     * @throws Exception
     */
    public static TreeMap<String, String> get_map(Path path) {
        TreeMap<String, String> map_workspace = new TreeMap<>();
        try {
            //使用Files.walk()遍历当前目录下的所有文件，使用filter()排除隐藏文件和文件夹。
            path=Paths.get(path.toAbsolutePath().toString().substring(utility.getPath().toString().length()));
            List<Path> files = Files.walk(path).filter((p) -> (!Files.isDirectory(p) && !(p.toString().charAt(0) == '.'))).collect(Collectors.toList());
            for (Path file : files) {
                if (!new File(file.toString()).isHidden()) {

                    String file_name = file.toString();

                    String content = readFile(file.toString());

                    String hash = str2Hash(content);

                    map_workspace.put(file_name, hash);
                }
            }
            return map_workspace;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return 所有的提交记录的commit_id
     * @throws Exception
     */
    public static HashSet<String> get_commits() {
        HashSet<String> commits = new HashSet<>();//用来存储所有的commit文件的名称
        try {
            //使用File类和File.listFiles()方法来获取所有在文件夹中的文件。
            File f = new File(utility.getCommitsPath().toString());
            File[] files = f.listFiles();
            if (files.length == 0) throw new NotCommittedException();
            for (File fi : files) {
                //如果是文件
                // 不打印隐藏文件
                if (!fi.isHidden()) {
                    String commitStr = fi.getName();
                    commits.add(commitStr);//增加commit_id记录
                }
            }

        } catch (NotCommittedException e) {
            System.err.println("Not committed yet.");
            System.exit(0);
        }
        return commits;
    }

    /**
     * 对比两个map是否修改文件或新增文件
     *
     * @return 如果有变化，返回true，否则返回false
     */
    public static boolean is_map_changed_added(TreeMap<String, String> map_1, TreeMap<String, String> map_2) {
        boolean flag = false;
        for (Map.Entry<String, String> entry1 : map_1.entrySet()) {
            for (Map.Entry<String, String> entry2 : map_2.entrySet()) {
                if (entry1.getKey().equals(entry2.getKey())) {
                    if (!entry1.getValue().equals(entry2.getValue())) {//对比map_1与map_2中的每个文件，若有不同则返回true
                        flag = true;
                    }
                }
            }
            if (!map_2.containsKey(entry1.getKey()))//检查map_1中是否有新增文件。
                flag = true;
        }
        return flag;
    }


}


