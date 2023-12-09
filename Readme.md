<a name="yAAWK"></a>
### 内部原理
- Git是主类
- Blob对象是对一个文件的抽象，保存着文件快照
- index文件保存暂存区信息，数据结构是将文件名（相对于git仓库主文件夹的相对路径）映射到文件的hash的Map
- tree 文件根据某一时刻暂存区（即 index 区域）所表示的状态创建并记录一个对应的树对象
- commit保存上一次commit ID和此次提交的tree ID，并将本次commit ID保存至refs文件夹中的对应分支名称文件
- 每次我们运行 git add 和 git commit 命令时，Git 所做的工作实质就是将被改写的文件保存为数据对象， 更新暂存区，记录树对象，最后创建一个指明了树对象和父提交的提交对象。

<a name="rmVbE"></a>
### 设计框架
  -.Mygit  存储一切<br />    -objects 存储commit、tree和blob对象（使用hashcode作为文件名）<br />     	-commits 文件夹存储每个commit对象<br />-commit记录用户每次commit的msg，时间信息，并对相关文件进行跟踪。每个commit实例都是commit tree上的一个节点。通过parent属性追踪上一个commit，通过head属性追踪其分支名称<br />     -存储每个blob和tree对象<br />- blob 将每个文件的哈希值和content以blob的形式存起来<br />- tree 存储此次提交commit的index文件快照<br />    -refs<br />     	-heads 存储分支末端（文件名为分支名，内容为对应commit的hashCode）<br />    - HEAD 存储当前commit 所在的分支名称<br />    - index 存储缓存区内容（文件名-文件最新版本的hashCode ）<br />   <br />![image.png](https://cdn.nlark.com/yuque/0/2023/png/32665762/1673527305413-5a08c06a-7cf0-4f5c-8885-47d7f81b87dc.png#averageHue=%23efefef&clientId=u3cea3ec8-207f-4&from=paste&height=815&id=ufcbb2de0&originHeight=815&originWidth=962&originalType=binary&ratio=1&rotation=0&showTitle=false&size=99143&status=done&style=none&taskId=ucbfd0bd9-14e4-423d-ba6c-045db66e0ec&title=&width=962)
<a name="ddF8x"></a>
### 项目亮点： 
这个项目是一个简单的版本控制系统的实现，亮点包括：

1. 分支管理功能: 支持了创建、切换、删除分支等操作。
2. 实现了文件夹操作: 支持对文件夹进行提交、删除等操作。
3. 支持绝大多数的版本控制功能: 如commit、checkout、status、diff、find，push/pull等功能。如：
   1.  commit功能：
      - 使用hash值维护每次的commit，可以保证每次commit的唯一性
      - 支持多级文件夹的commit，并能对工作区和缓存区进行区分
      - 支持对commit进行查询，能快速找到指定commit。
      - 支持对commit进行分支，能在不同版本上进行操作
      - 支持对commit进行回滚，能在不同版本之间切换。
   2. rm 功能：
      - 支持删除文件和文件夹，并能够在删除文件时对比工作区和缓存区文件的内容，确保删除的文件是最新的版本。
      - 支持使用 "--f" 和 "--cached" 选项强制删除文件，这样可以在文件已经被修改或者已经被添加到缓存区的情况下强制删除文件。
      - 在删除文件时，提供了足够的异常处理，如文件不存在，文件未被缓存等，能够有效地提示用户错误信息。
      - 支持递归删除文件夹，能够删除文件夹内的所有文件。
   3. push/pull可以将本地版本库的内容与远程版本库的内容进行同步，大大简化团队协作的流程，提高协作效率。
4. 完成了额外的操作，如reflog/status/diff/分支管理 /find /checkout等功能。
5. 熟练运用IO流，使用了FileWriter、ObjectOutputStream等IO流来实现对文件的读写操作，方便地实现了读写文件的操作。
6. 在每个操作函数中都进行了相应的异常处理，可以很好地保证程序在出现错误时继续执行并给出相应的提示信息。
<a name="HHRCy"></a>
### 各类的属性及方法：
类图：<br />![git.png](https://cdn.nlark.com/yuque/0/2023/png/32665762/1673620327463-1a245626-5eb9-463c-a165-0071529045e8.png#averageHue=%23b29655&clientId=u83cc85ba-4e38-4&from=ui&id=u7d5a5c03&originHeight=3458&originWidth=3040&originalType=binary&ratio=1&rotation=0&showTitle=false&size=768291&status=done&style=none&taskId=ue4149508-dbbf-4923-977f-a1c11d904f0&title=)
<a name="AC9gd"></a>
#### blob类
<a name="f6m0A"></a>
##### 属性：
| 名称 | 作用 |
| --- | --- |
| private final String type | 对象类型  |
| private final int size | 内容字符数  |
| private final String content | 文件内容  |


<a name="z0AKA"></a>
##### 方法：
| 名称 | 作用 |
| --- | --- |
| public Blob(String content)  {...} | 构造函数 |
| public static String get_Blob_path (Blob blob) {...} | 返回blob对象的所在路径  |
| public static void writeBlob(List<Path> files)  {...} | 在objects文件夹中保存blob对象  |
| public static Blob deSerialFrom(String path)  {...} | 从位于指定路径的文件中反序列化 blob 对象 |
| public String getContent()  {...}  | 返回blob保存的文件内容 	 |
| public String toString()  {...} | 返回blob对象内容 	 |

<a name="lP04j"></a>
#### index类：
<a name="pA48h"></a>
##### 属性：
| 名称 | 作用 |
| --- | --- |
| private TreeMap<String, String> tracking  | 将文件名映射到文件最新版本的哈希值的treemap  |

<a name="xq7CT"></a>
##### 方法：
| 名称 | 作用 |
| --- | --- |
| public Index(TreeMap<String, String> tracking) {...} | index 构造函数  |
| public static void writeIndex(List<Path> files)  {...} | 创建 当前工作区快照构成的treemap 并将其写入文件  |
| public static Index deSerialFrom() {...} | 用于从位于指定路径的文件中反序列化 index 对象。  |
| public TreeMap<String, String> getTracking() {...} | 返回index的 TreeMap 属性  |

<a name="XZsyG"></a>
#### tree类：
<a name="kYbQ5"></a>
##### 属性：
| 名称 | 作用 |
| --- | --- |
| private static int size | tree对象的字符数  |
| private final String type | 对象的类型  |
| private final TreeMap<String, String> tracking | 当前工作区快照构成的treemap  |

<a name="UTbRJ"></a>
##### 方法：
| 名称 | 作用 |
| --- | --- |
| public static Tree addTree()   | 创建 当前工作区快照构成的treemap 并将其写入文件  |
| public static Tree deSerialFrom(String path)  | 用于从位于指定路径的文件中反序列化 Tree 对象 |
| public static Tree get_head_tree()  | 返回当前分支的最末端commit的tree对象  |
| public static Tree get_tree(Commit commit)  | 通过commit 对象 查找该次commit提交的tree对象  |
| public TreeMap<String, String> getTracking()  | 返回当前tree 跟踪的index副本   |
| public String toString()  | 返回tree对象的字符串表示形式  |

<a name="az6SV"></a>
#### commit 类：
<a name="AoNzI"></a>
##### 属性：
| 名称 | 作用 |
| --- | --- |
| private final String parent | parent commit的commitStr  |
| private final String type | 对象类型  |
| private final String treeHash | 储存本次commit的文件目录  |
| private final String author | commit的作者  |
| private final ZonedDateTime timestamp | 时间戳信息  |
| private final String message | 提交信息  |

<a name="bf4YF"></a>
##### 方法：
| 名称 | 作用 |
| --- | --- |
| public Commit(String parent, String treeHash, String author, ZonedDateTime timestamp, String message)   | 构造函数  |
| public static void addCommit(String parent, String message, Tree tree, ZonedDateTime timestamp, String author)   | 将本次提交的commit对象序列化到objects文件夹下  |
| public static Commit deSerialFrom(String path)  | 用于从位于指定路径的文件中反序列化 commit对象 |
| public static String getCommitStr(Commit commit)  | 返回以字符串格式传递的commit的哈希值  |
| public static Commit getCommit(String commit_id)  | 通过commit的哈希值 查找具有该哈希值名称的文件并反序列化该文件中的对象。  |
| public static String get_head_commit_id()  | 返回当前分支的最末端commit的哈希值  |
| public static Commit get_head_commit()  | 返回当前分支的最末端commit对象  |
| public String getTreeHash()   | 返回该次提交的的tree 对象的哈希值  |
| public String get_parent()   | 返回该次提交的parent commit哈希值  |
| public String toString()  | 返回commit对象的字符串表示形式  |
| public String getMessage()   | 返回commit对象的提交信息  |

<a name="MyVJs"></a>
#### server类：
<a name="QKls3"></a>
##### 方法：
| 名称 | 作用 |
| --- | --- |
| public static void main(String[] args)   | 创建服务器  |

<a name="ttq6j"></a>
#### utility类：
<a name="DNyVA"></a>
##### 方法：
| 名称 | 作用 |
| --- | --- |
| public static Path getPath()  | 获取当前所在路径  |
| public static Path getGitDirPath()  | 获取Mygit文件夹所在路径  |
| public static Path getIndexFilePath()  | 获取index文件夹所在路径  |
| public static Path getFilesPath()  | 获取objects文件夹所在路径  |
| public static Path getCommitsPath()  | 获取commits文件夹所在路径  |
| public static Path getHeadPath()  | 获取HEAD文件夹所在路径  |
| public static Path getRefsPath()  | 获取refs文件夹所在路径  |
| public static String str2Hash(String str)  | 计算字符串的sha-1值  |
| public static void deleteDir(Path path)  | 用于删除文件夹下所有非隐藏文件的方法  |
| public static void delete_allDir(Path path)  | 用于删除文件夹及其所有内容的方法  |
| public static String readFile(String filePath)  | 用于读取文件内容的方法  |
| public static String get_branch()   | 返回当前分支的名称  |
| public static void unzip(String zipFilePath, String desDirectory)   | 解压文件  |
| public static void mkdir(File file)  | 用来创建文件夹的方法  |
| public static void zipFileTree(File sourceFile)  | 压缩文件或文件夹（包括所有子目录文件）  |
| public static void zip(File file, ZipOutputStream zos, String relativePath)  | 递归压缩文件  |
| public static void receive(Socket socket)  | 用于接收文件  |
| public static void send(Socket socket)  | 用于发送文件  |
| public static void sendMessage(Socket socket, String str)  | 用给定的 socket 发送一条字符串消息  |
| public static String getMessage(Socket socket)  | 从给定的 socket 上读取一条字符串消息  |
| public static Socket start_client()   | 创建一个客户端  |
| public static TreeMap<String, String> get_map(Path path)  | 返回当前路径文件状态  |
| public static HashSet<String> get_commits()   | 返回所有的提交记录的commit_id  |
| public static boolean is_map_changed_added(TreeMap<String, String> map_1, TreeMap<String, String> map_2)  | 对比两个map是否修改文件或新增文件  |

<a name="puHtw"></a>
#### Exceptions类：
<a name="rHeSI"></a>
##### 异常类
| 名称 | 作用 |
| --- | --- |
| AlreadyExistBranchException  | 处理生成重名分支异常 |
| DeleteCurrentBranchException |  处理删除当前分支异常 |
| NoSuchBranchException  | 处理未找到分支异常 |
| NoSuchCommitException  | 处理未找到该commit异常 |
| NoSuchFileException | 处理未找到该文件异常 |
| NotCommittedException  | 处理未提交异常 |
| NotIndexedException | 处理未暂存异常 |

<a name="TzCIi"></a>
#### git类：
<a name="ZldBQ"></a>
##### 方法：
| 名称 | 作用 |
| --- | --- |
| public static void main(String[] args)   | 主函数 |
| public static void checkArgsValid(String[] args, int argsLength)   | 判断用户参数输入是否正确  |
| public static boolean isInitialized()  | 判断是否已经初始化仓库  |
| public static void init/add/...(String[] args)  | 操作命令 |

<a name="mn8qH"></a>
### 实现功能
![image.png](https://cdn.nlark.com/yuque/0/2023/png/32665762/1673511603253-a0fd196b-f5fd-4eda-be23-7418e6553211.png#averageHue=%23f9f8f5&clientId=u3cea3ec8-207f-4&from=paste&height=488&id=u697da3ae&originHeight=488&originWidth=783&originalType=binary&ratio=1&rotation=0&showTitle=false&size=47279&status=done&style=none&taskId=u9b11eafe-d16b-4b81-8f06-bedd52d31f8&title=&width=783)
<a name="tQtb1"></a>
#### 1.在当前目录初始化.Mygit仓库：
:::info
<a name="n3xoQ"></a>
#### java  git init 
:::
<a name="xsk4U"></a>
##### 实现原理：
创建文件夹: .Mygit 用于记录git仓库

1. 判断工作区中（当前路径）是否存在.Mygit目录，已存在则打印提示信息；
2. 创建.Mygit目录，在其中创建objects目录用于blob、tree等对象；objects文件夹里创建commits文件夹，保存commit文件快照，命名均为其hash值
3. 创建Index对象序列化到.git目录下用于储存 文件名-hash值的对应条目（初始为空）；
4. 创建HEAD文件储存目前被检出的分支末端的commit id（初始为空）。
5. 创建refs文件夹，保存各分支名字的文件，内容是分支末端的commit_id
<a name="lla20"></a>
##### 异常：
如果已经有.gitlet文件存在，输出错误信息：<br />A git.Mygit version-control system already exists in the current directory 
<a name="s9VM7"></a>
#### 2.跟踪或者暂存最新版文件或文件夹
:::info
<a name="ln0XT"></a>
#### java git add [filename]<br />java git add .
:::
<a name="jpgcW"></a>
##### 实现原理：

1. 读取args[1],如果是"."表示为暂存所有工作区文件，否则只暂存对应文件<br />（1）当暂存所有工作区文件，创建工作区所有blob对象（blob+" "+内容字符数+ 内容），对象名字为对应的hash值，序列化到objects文件夹下，清空index文件内容,，并存储文件名-对应blob的hash值入index<br />（2）当暂存对应文件，创建对应blob对象序列化到objects文件夹下，读取当前index文件内容，新增对应文件名-hash值 条目，并保存新的index文件
2. 当add的文件，不存在于工作区时，<br />（1）遍历index文件条目，判断是否存在同名文件或文件夹，如果存在，在暂存区中删除对应条目；<br />（2）如果不存在，输出异常
<a name="gPNrd"></a>
##### 异常：
如果该文件不存在，输出错误信息：<br />No file with that name exists 。
<a name="iGryP"></a>
#### 3.提交暂存区
:::info
<a name="NHSI9"></a>
#### java git commit --m [message]
:::
<a name="IkJfz"></a>
##### 实现原理：

1. 将index中所有条目生成tree对象序列化到objects文件夹下；<br />（1）建立tree文件 读取当前index的TreeMap条目，并存入tree文件对应格式的TreeMap，内容是 tree blob 文件名 hash值
2. 将commit对象序列化到objects文件夹下，commit对象包括以下属性：parent commit id、本次commit所生成tree id，message、时间戳；

(1)读取head文件中上一次的commit id，判断parent commit id和本次commit中的tree id是否一致，如果一致，则输出异常<br />(2) 如果index文件为空，输出异常<br />(3）创建commit-对象。如果该commit的parent commit_id为空（刚初始化），所属分支为master,否则该Commit对象所属分支为其parent 分支

3. 如果是第一次提交，更新HEAD文件中的分支名称。
4.  在refs中保存当前branch的commit_id
5. 通过比较两次tree中储存的文件目录，打印本次commit相对上一次commit的文件变动情况（增加、删除、修改）
<a name="HFs0a"></a>
##### 异常：
如果暂存区中没有内容，输出错误信息：<br />Not indexed yet. <br />如果本次提交与上次提交的文件内容没有变化，输出错误信息：<br />No changes added to the commit.
<a name="PwHMy"></a>
#### 4.将指定文件或文件夹从暂存区删除，同时也在磁盘上删除该文件
:::info
<a name="MSgPK"></a>
#### java git rm [filename]
<a name="kGL05"></a>
##### java git rm --cached  [filename]
<a name="SSxyh"></a>
##### java git rm --f [filename]
:::
<a name="Q5Zet"></a>
##### 实现原理： 

1. 判断文件是否存在于工作区，如果不存在，输出提醒信息
2. 判断文件名是否存在于暂存区，如果不存在，输出提醒信息
3. git rm --cached 命令： 删除暂存区同名文件，但保留工作区的文件。
4. git rm -f 命令：删除工作区和暂存区同名文件，要删除的文件和当前版本库文件的内容相同或不同都可以
5. git rm 命令：当同名文件和当前版本库文件的内容相同时，删除工作区和暂存区同名文件，否则输出提醒信息

（1）当同名文件与暂存区同名文件内容相同时，提醒该文件新版本未commit到版本库，使用 --cached 或 --f命令删除文件<br />（2）当同名文件与暂存区同名文件内容不相同时，提醒该文件新版本未更新到暂存区，使用 --cached  或--f 命令删除文件

6. 如果是文件夹，删除暂存区中与文件夹文件名相同的键值对 

<a name="zIMpm"></a>
##### 异常：
如果该文件不存在于磁盘中，输出错误信息：<br />No file with this name exists. <br />如果该文件不存在于暂存区中，输出错误信息：<br />Not indexed yet. 
<a name="Tq3XX"></a>
#### 5.按照时间逆序打印当前分支的所有历史提交记录，直到第一次提交
:::info
<a name="H9rn7"></a>
#### java git log 
:::
<a name="sM4NR"></a>
##### 实现原理：
 1.从HEAD文件中读取当前分支，若HEAD为空打印提示信息。<br /> 2.在refs文件夹中找到当前分支的末端commit,打印parent commit id，author,message，commit时间等信息<br /> 3.读出该commit中存放的前一次commit id，<br /> 4.反复执行2、3直到打印完分支所有commit的内容。
<a name="CfBWH"></a>
##### 异常：
如果该repo尚未commit，输出错误信息：<br />No commit yet .
<a name="TTw15"></a>
#### 6.检出到指定提交
:::info
<a name="I0i9a"></a>
#### java git reset [commit_id]
:::
<a name="E58tF"></a>
##### 实现思路：

1. 取出args[] 中的commit id，以及reset模式，其中mixed是默认模式， 
2. 判断objects文件夹中是否存在对应的commit对象， 
3. 判断reset的形式：

-- soft 修改当前branch文件内容为给定commit id <br />-- mixed 在soft基础上反序列化该commit id中的tree ，根据tree的内容重置暂存区<br />-- hard 在mixed基础上，首先清空工作区，读取暂存区的文件名和对应的blob储存内容，新建文件
<a name="VyvsY"></a>
##### 异常：
如果没有对应的commit存在，打印错误信息：<br />No such commit exists. <br />如果工作目录存在未被commit跟踪，且将被覆写的文件，输出错误信息 :<br />There is an untracked file in the way; delete it, or add and commit it first.
<a name="GegvB"></a>
#### 7.下载/上传远程代码
:::info
<a name="JqUdX"></a>
#### java git pull
<a name="h1xxe"></a>
#### java git push
:::
<a name="AjSZb"></a>
##### 实现思路：
**pull : 使用socket在从远程存储库中拉取更改，并将本地工作区重置**

1. 启动客户端套接字，并将返回的套接字分配给“socket_client”变量。
2. 客户端向服务器发送消息“pull”告知服务器发送文件
3. 删除当前本地 git 版本库。
4. 服务器用ZipOutputStream将版本库压缩并传输，客户端接收服务器的版本库并解压
5. 本地根据存储库的上一次提交重置工作区

 <br />**push : 使用socket将更改推送到远程存储库，并将远程工作区重置**

1. 启动客户端套接字，并将返回的套接字分配给“socket_client”变量。
2. 客户端向服务器发送消息“push”告知服务器接收文件
3. 删除远程存储哭的 git 版本库。
4. 客户端用ZipOutputStream将版本库压缩并传输，服务器接收客户端的版本库并解压
5. 远程根据存储库的上一次提交重置工作区
<a name="mU9Ps"></a>
##### 异常：
如果服务器未启动时，先启动客户端，输出错误信息：<br />请先启动服务器<br />如果端口号已被占用，输出错误信息：<br />端口号已被使用，请重新输入~~ <br />如果执行pull操作时，远程仓库无内容，输出错误信息：<br />当前仓库为空，请先建立仓库 

<a name="tr0w8"></a>
#### 8.将当前工作树与暂存区中的文件进行比较，并打印出差异。 
:::info
<a name="ADAu0"></a>
#### java git diff
:::
<a name="iRfM7"></a>
##### 实现思路：
获取记录当前工作区文件状态的treemap和index 的treemap，对比键值判断是否文件内容是否修改，或新增删除文件 
<a name="HYrOI"></a>
#### 9.添加分支
:::info
<a name="C5C2R"></a>
####  java git branch [branch name]
:::
<a name="XP7w7"></a>
##### 实现思路：

1. 检查代表输入参数指定的分支的目录路径是否已经存在，如果是，它会抛出“AlreadyExistBranchExceptio
2. 在 refs 目录中创建一个新文件，代表新分支，名称由输入参数指定，并将当前提交commit的哈希值写入其中。
3. 使用 FileWriter 对象用新的分支名称更新头文件
<a name="DjFRo"></a>
##### 异常：
如果给定的branch已存在，输出错误信息：<br />A branch with that name already exists. 
<a name="o2dgK"></a>
#### 10.检出到指定分支
:::info
<a name="J7h4k"></a>
#### java git checkout [branch name]
:::
<a name="tY5nM"></a>
##### 实现思路：

1. 检查代表输入参数指定的分支的目录路径是否存在，如果不存在，则抛出“NoSuchBranchException”
2. 检查工作目录是否有任何未跟踪的更改，如果有，输出错误信息
3. 检查当前分支是否与输入参数相同，如果是则抛出错误消息并退出
4. 使用 FileWriter 对象将头文件更新为新的分支名称
5. 使用 reset(list) 方法更新到新指定分支的最新提交
6. 打印一条消息，表明分支已被切换。
<a name="BugV5"></a>
##### 异常：
如果checked branch不存在，输出错误信息：<br />No such branch exists.<br />如果checked branch就是当前分支，输出错误信息：<br />No need to checkout the current branch.<br />如果工作目录存在未被commit跟踪，且将被覆写的文件，输出错误信息 <br />There is an untracked file in the way; delete it, or add and commit it first.

<a name="LoSeY"></a>
#### 11.打印当前状态
:::info
<a name="g32ef"></a>
#### java git status
:::
<a name="MGnMP"></a>
##### 实现思路：
打印现存的分支名称，在当前分支的前面加上*号；<br />通过对比工作区与缓存区储存内容，打印状态，分为三种：

1. 跟踪中的文件
2. 已经暂存但是在工作区已经被修改或者删除的文件
3. 工作目录中没有被跟踪的文件
<a name="KUGHr"></a>
#### 12.删除指定分支
:::info
<a name="cTv5b"></a>
#### java git rmBranch  [branch name]
:::
<a name="XGMks"></a>
##### 实现思路： 

1. 检查被删除的分支是否为当前分支，如果是则抛出异常并退出程序。
2. 如果分支不存在，抛出异常并退出程序。
3. 如果该分支存在，删除 refs 目录中代表该分支的文件。
<a name="tgNuW"></a>
##### 异常：
如果给定的branch不存在，输出错误信息：<br />A branch with that name does not exist.<br />如果尝试删除的branch为当前branch，输出错误信息：<br />Cannot remove the current branch.
<a name="lByri"></a>
#### 13.打印出本仓库的所有提交记录
:::info
<a name="I2SZ3"></a>
#### java git reflog 
:::
<a name="IOf0M"></a>
##### 实现思路：
获取commits文件夹中所有的commit对象并打印
<a name="dAWhW"></a>
##### 异常：
当commits文件夹为空，打印Not committed yet. 
<a name="FHE4F"></a>
#### 14.打印出所有message为给定message的全部提交记录
:::info
<a name="xkW4n"></a>
#### java git find [commit message]
:::
<a name="Jv0Oi"></a>
##### 实现思路：

1. 将变量 noSuchCommit 初始化为 true，并调用方法 get_commits() 创建存储库中所有提交的 HashSet。
2. 遍历提交 HashSet 中的每个提交，如果提交的消息与作为参数传入的消息匹配，它会打印提交对象并将 noSuchCommit 设置为 false。
3. 在遍历所有提交之后，检查 noSuchCommit 的值，如果它仍然为真，则意味着没有找到具有给定消息的提交,输出错误信息。
<a name="uo6ZV"></a>
##### 异常：
如果没有对应的commit存在，打印错误信息：<br />Found no commit with that message. 

<a name="AJubE"></a>
### 心得体会：
在完成这个项目后，我获得了很多实际开发经验。<br />首先，我对版本控制的原理有了更深入的理解，包括工作区、暂存区、版本库之间的关系和它们之间的数据传输，并且学会了如何使用各种操作来实现版本控制功能。<br />其次，我学会了如何使用IO流来读写文件，这是实现版本控制的基础。我熟练地使用了ObjectOutputStream、FileWriter、FileInputStream等流来实现文件读写操作。<br />此外，在完成这个项目过程中，我学会了如何处理异常，并且学会了如何使用try-catch来处理异常。这对我来说是一个很重要的技能，因为在实际开发中，异常处理是必不可少的。<br />最后，通过这个项目，我深刻地意识到了版本控制系统的重要性，它不仅可以帮助我们管理代码版本，还可以帮助我们管理代码的修改历史。

