package com.example.poemai.service;

import android.content.Context;
import android.util.Log;

import com.example.poemai.database.DatabaseHelper;
import com.example.poemai.utils.JwtUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简化的后端服务类，直接在Android应用中提供后端功能
 */
public class BackendService {
    private static final String TAG = "BackendService";
    private static BackendService instance;

    private Gson gson;
    private Context context;
    private DatabaseHelper databaseHelper;

    // 模拟数据库存储
    private Map<String, User> users = new HashMap<>();
    private List<CiPai> ciPais = new ArrayList<>();
    private Map<String, RhymeGroup> rhymeGroups = new HashMap<>();

    private BackendService(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new GsonBuilder().create();
        this.databaseHelper = new DatabaseHelper(this.context);
        initializeData();
        initializeUsers();
    }

    public static synchronized BackendService getInstance(Context context) {
        if (instance == null) {
            instance = new BackendService(context);
        }
        return instance;
    }

    private void initializeUsers() {
        // 初始化示例用户（密码为123456）
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setPasswordHash("MTIzNDU2"); // 123456的Base64编码
        users.put("admin", admin);
    }

    /**
     * 用户注册
     * @param username 用户名
     * @param password 密码
     * @return 注册结果
     */
    public Result<Map<String, Object>> register(String username, String password) {
        try {
            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                return Result.<Map<String, Object>>error("用户名和密码不能为空");
            }

            // 检查用户是否已存在
            if (users.containsKey(username)) {
                return Result.<Map<String, Object>>error("用户已存在");
            }

            // 创建新用户
            User user = new User();
            user.setId((long) (users.size() + 1));
            user.setUsername(username);
            user.setPasswordHash(password); // 简化处理，实际应该加密

            // 保存到数据库
            databaseHelper.insertUser(user);

            users.put(username, user);

            String token = JwtUtils.generateToken(user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());

            return Result.success(response);
        } catch (Exception e) {
            Log.e(TAG, "Registration error", e);
            return Result.<Map<String, Object>>error("注册失败: " + e.getMessage());
        }
    }

    // 用户登录
    public Result<Map<String, Object>> login(String username, String password) {
        try {
            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                return Result.<Map<String, Object>>error("用户名和密码不能为空");
            }

            // 查找用户
            User user = users.get(username);
            if (user == null) {
                // 从数据库中查找用户
                user = databaseHelper.getUserByUsername(username);
                if (user != null) {
                    // 缓存到内存中
                    users.put(username, user);
                }
            }

            if (user == null) {
                return Result.<Map<String, Object>>error("用户不存在");
            }

            // 验证密码（简化处理，实际应该使用加密）
            if (!user.getPasswordHash().equals(password)) {
                return Result.<Map<String, Object>>error("密码错误");
            }

            String token = JwtUtils.generateToken(user.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());

            return Result.success(response);
        } catch (Exception e) {
            Log.e(TAG, "Login error", e);
            return Result.<Map<String, Object>>error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 根据长度匹配词牌
     * @param lengths 二维长度列表，例如[[5,5],[7,7,7,7]]
     * @return 匹配的词牌列表
     */
    public Result<List<CiPai>> matchCiPaiByLengths(List<Integer> lengths) {
        try {
            if (lengths == null || lengths.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            List<CiPai> matchedCiPais = new ArrayList<>();

            // 将整数列表转换为格式字符串
            String requiredFormat = lengths.toString().replace(" ", "");

            for (CiPai cipai : ciPais) {
                String[] sentenceLengths = cipai.getSentenceLengths();

                // 在词牌的sentenceLengths中查找匹配的格式
                boolean found = false;
                for (String storedFormat : sentenceLengths) {
                    storedFormat = storedFormat.replace(" ", "");
                    if (requiredFormat.equals(storedFormat)) {
                        found = true;
                        break;
                    }
                }

                // 如果找到匹配的格式，则添加到结果中
                if (found) {
                    matchedCiPais.add(cipai);
                }
            }

            return Result.success(matchedCiPais);
        } catch (Exception e) {
            Log.e(TAG, "Match ci pai error", e);
            return Result.<List<CiPai>>error("匹配词牌失败: " + e.getMessage());
        }
    }


    /**
     * 保存作品到数据库
     * @param workData 作品数据
     * @param userId 用户ID
     * @return 保存结果
     */
    public Result<Map<String, Object>> saveWork(Map<String, Object> workData, Long userId) {
        try {
            Log.d(TAG, "saveWork called with userId: " + userId);

            if (userId == null) {
                return Result.error("用户未登录");
            }

            if (workData == null || workData.isEmpty()) {
                return Result.error("作品数据为空");
            }

            // 创建作品对象
            DatabaseHelper.Work work = new DatabaseHelper.Work();
            work.setUserId(userId);
            work.setTitle((String) workData.get("title"));
            work.setContent((String) workData.get("content"));
            work.setWorkType((String) workData.get("workType"));

            // 处理背景信息
            Object backgroundInfo = workData.get("backgroundInfo");
            if (backgroundInfo instanceof Map) {
                work.setBackgroundInfo((Map<String, Object>) backgroundInfo);
            }

            // 保存到数据库
            long workId = databaseHelper.insertWork(work);

            if (workId > 0) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("id", workId);
                resultData.put("message", "作品保存成功");
                return Result.success(resultData);
            } else {
                return Result.error("作品保存失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "saveWork error", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有作品
     * @param userId 用户ID
     * @return 作品列表
     */
    public Result<List<DatabaseHelper.Work>> getAllWorks(Long userId) {
        try {
            Log.d(TAG, "getAllWorks called with userId: " + userId);

            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 从数据库获取用户的所有作品
            List<DatabaseHelper.Work> works = databaseHelper.getAllWorksByUserId(userId);

            return Result.success(works);
        } catch (Exception e) {
            Log.e(TAG, "getAllWorks error", e);
            return Result.error("获取作品列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除作品
     * @param workId 作品ID
     * @param userId 用户ID
     * @return 删除结果
     */
    public Result<String> deleteWork(long workId, Long userId) {
        try {
            Log.d(TAG, "deleteWork called with workId: " + workId + ", userId: " + userId);

            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 从数据库删除作品
            int deletedRows = databaseHelper.deleteWorkByIdAndUserId(workId, userId);

            if (deletedRows > 0) {
                return Result.success("作品删除成功");
            } else {
                return Result.error("作品删除失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "deleteWork error", e);
            return Result.error("删除作品失败: " + e.getMessage());
        }
    }

    /**
     * 根据作品ID获取作品详情
     * @param workId 作品ID
     * @param userId 用户ID
     * @return 作品详情
     */
    public Result<DatabaseHelper.Work> getWorkById(long workId, Long userId) {
        try {
            Log.d(TAG, "getWorkById called with workId: " + workId + ", userId: " + userId);

            if (userId == null) {
                return Result.error("用户未登录");
            }

            // 从数据库获取作品详情
            DatabaseHelper.Work work = databaseHelper.getWorkById(workId);

            // 检查作品是否属于该用户
            if (work != null && work.getUserId().equals(userId)) {
                return Result.success(work);
            } else if (work != null) {
                return Result.error("无权访问该作品");
            } else {
                return Result.error("作品不存在");
            }
        } catch (Exception e) {
            Log.e(TAG, "getWorkById error", e);
            return Result.error("获取作品详情失败: " + e.getMessage());
        }
    }

    // 获取押韵字
    public Result<Map<String, Object>> getRhymeInfoByChar(String query) {
        try {
            if (query == null || query.isEmpty()) {
                return Result.<Map<String, Object>>error("请输入查询字");
            }

            // 获取查询字的拼音
            String pinyin = convertToPinyin(query);
            if (pinyin == null || pinyin.isEmpty()) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("rhymeGroup", "未找到");
                emptyResult.put("words", new String[]{});
                emptyResult.put("message", "未找到字符 '" + query + "' 对应的押韵组");
                return Result.success(emptyResult);
            }

            // 提取韵母
            String rhymeFinal = extractRhymeFinal(pinyin);

            // 在押韵组中查找匹配的韵母
            RhymeGroup matchedGroup = null;
            for (RhymeGroup group : rhymeGroups.values()) {
                if (group.getGroupName().contains(rhymeFinal)) {
                    matchedGroup = group;
                    break;
                }
            }

            if (matchedGroup == null) {
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("rhymeGroup", "未找到");
                emptyResult.put("words", new String[]{});
                emptyResult.put("message", "未找到字符 '" + query + "' 对应的押韵组");
                return Result.success(emptyResult);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("rhymeGroup", matchedGroup.getGroupName());
            result.put("words", gson.fromJson(matchedGroup.getCharacterList(), List.class));

            return Result.success(result);
        } catch (Exception e) {
            Log.e(TAG, "Rhyme words error", e);
            return Result.<Map<String, Object>>error("查询失败: " + e.getMessage());
        }
    }

    // 获取所有词牌
    public List<CiPai> getAllCiPais() {
        if (ciPais.isEmpty()) {
            // 初始化词牌数据
            initializeCiPaiData();
        }
        return ciPais;
    }

    // 初始化词牌数据
    private void initializeData() {
        initializeCiPaiData();
        initializeRhymeData();
    }

    private void initializeCiPaiData() {
        // 清空现有数据
        ciPais.clear();

        // 添加词牌数据
        addCiPai("水调歌头", "明月几时有？把酒问青天。不知天上宫阙，今夕是何年。我欲乘风归去，又恐琼楼玉宇，高处不胜寒。起舞弄清影，何似在人间。", new String[]{"[5,5]", "[6,5]", "[6,6,5]","[5,5]","[3,3,3]","[4,7]","[6,6,5]","[5,5]","[6,6]"});
        addCiPai("临江仙", "滚滚长江东逝水，浪花淘尽英雄。是非成败转头空。青山依旧在，几度夕阳红。\n白发渔樵江渚上，惯看秋月春风。一壶浊酒喜相逢。古今多少事，都付笑谈中。", new String[]{"[7,6]","[7]","[5,5]","[7,5,5]"});
        addCiPai("念奴娇", "大江东去，浪淘尽，千古风流人物。\n故垒西边，人道是，三国周郎赤壁。\n乱石穿空，惊涛拍岸，卷起千堆雪。江山如画，一时多少豪杰。\n遥想公瑾当年，小乔初嫁了，雄姿英发。\n羽扇纶巾，谈笑间，樯橹灰飞烟灭。\n故国神游，多情应笑我，早生华发。\n人生如梦，一尊还酹江月。", new String[]{"[4,3,6]","[4,3,6]","[4,4,5]","[4,6]","[6,5]","[4,4]","[3,6]","[4,5,4]","[4,6]","[6,5,4,4,3,6]","[4,7]","[4,4,6]"});
        addCiPai("沁园春", "北国风光，千里冰封，万里雪飘。\n望长城内外，惟余莽莽；大河上下，顿失滔滔。\n山舞银蛇，原驰蜡象，欲与天公试比高。\n须晴日，看红装素裹，分外妖娆。\n江山如此多娇，引无数英雄竞折腰。\n惜秦皇汉武，略输文采；唐宗宋祖，稍逊风骚。\n一代天骄，成吉思汗，只识弯弓射大雕。\n俱往矣，数风流人物，还看今朝。", new String[]{"[4,4,4]","[5,4]","[4,4]","[4,4,7]","[4,4]","[3,5,4]","[6,8]","[5,4]","[5,4,4,4]","[4,4,7]","[4,5,4]"});
        addCiPai("江城子", "十年生死两茫茫，不思量，自难忘。千里孤坟，无处话凄凉。纵使相逢应不识，尘满面，鬓如霜。\n夜来幽梦忽还乡，小轩窗，正梳妆。相顾无言，惟有泪千行。料得年年肠断处，明月夜，短松冈。", new String[]{"[7,3,3]","[4,5]","[7,3,3]","[7,3,3,4,5]"});
        addCiPai("满江红", "怒发冲冠，凭栏处、潇潇雨歇。抬望眼，仰天长啸，壮怀激烈。三十功名尘与土，八千里路云和月。莫等闲，白了少年头，空悲切\n靖康耻，犹未雪。臣子恨，何时灭！驾长车，踏破贺兰山缺。壮志饥餐胡虏肉，笑谈渴饮匈奴血。待从头、收拾旧山河，朝天阙。", new String[]{"[4,3,3]","[3,4,4]","[7,7]","[3,5,3]","[3,3]","[3,3]","[3,6]","[7,7]","[3,5,3]","[4,3,4,3]","[3,3,3,3]"});
        addCiPai("青玉案", "东风夜放花千树，更吹落、星如雨。宝马雕车香满路。凤箫声动，玉壶光转，一夜鱼龙舞。\n蛾儿雪柳黄金缕，笑语盈盈暗香去。众里寻他千百度。蓦然回首，那人却在，灯火阑珊处。", new String[]{"[7,3,3]","[7]","[4,4,5]","[7,7]","[7,4,4,5]","[3,3]","[7,4,4,5]"});
        addCiPai("虞美人", "春花秋月何时了？往事知多少。小楼昨夜又东风，故国不堪回首月明中。\n雕栏玉砌应犹在，只是朱颜改。问君能有几多愁？恰似一江春水向东流。", new String[]{"[7,5]","[7,9]","[7]","[9]","[5]"});
        addCiPai("声声慢", "寻寻觅觅，冷冷清清，凄凄惨惨戚戚。乍暖还寒时候，最难将息。三杯两盏淡酒，怎敌他、晚来风急！雁过也，正伤心，却是旧时相识。\n满地黄花堆积，憔悴损，如今有谁堪摘？守着窗儿，独自怎生得黑？梧桐更兼细雨，到黄昏、点点滴滴。这次第，怎一个愁字了得！", new String[]{"[4,4,6]","[6,4]","[6,3,4]","[3,3,6]","[6,3,6]","[4,6]","[6,3,4]","[3,7]","[7]"});
        addCiPai("定风波", "莫听穿林打叶声，何妨吟啸且徐行。竹杖芒鞋轻胜马，谁怕？一蓑烟雨任平生。\n料峭春风吹酒醒，微冷，山头斜照却相迎。回首向来萧瑟处，归去，也无风雨也无晴。", new String[]{"[7,7]","[7,2]","[7]","[7,2,7]","[2]","[2,7]"});
        addCiPai("破阵子", "醉里挑灯看剑，梦回吹角连营。八百里分麾下炙，五十弦翻塞外声，沙场秋点兵。\n马作的卢飞快，弓如霹雳弦惊。了却君王天下事，赢得生前身后名。可怜白发生！", new String[]{"[6,6]","[7,7,5]","[7,5]","[5]","[7,7]"});
        addCiPai("卜算子", "驿外断桥边，寂寞开无主。已是黄昏独自愁，更着风和雨。\n无意苦争春，一任群芳妒。零落成泥碾作尘，只有香如故。", new String[]{"[5,5]","[7,5]","[7]","[5]"});
        addCiPai("雨霖铃", "寒蝉凄切，对长亭晚，骤雨初歇。都门帐饮无绪，留恋处，兰舟催发。执手相看泪眼，竟无语凝噎。念去去，千里烟波，暮霭沉沉楚天阔。\n多情自古伤离别，更那堪，冷落清秋节！今宵酒醒何处？杨柳岸，晓风残月。此去经年，应是良辰好景虚设。便纵有千种风情，更与何人说？", new String[]{"[4,4,4]","[6,3,4]","[6,5]","[3,4,7]","[7,3,5]","[6,3,4]","[4,8]","[7,5]","[4,4]","[3,4]","[7]"});
        addCiPai("鹊桥仙", "纤云弄巧，飞星传恨，银汉迢迢暗度。金风玉露一相逢，便胜却人间无数。\n柔情似水，佳期如梦，忍顾鹊桥归路。两情若是久长时，又岂在朝朝暮暮。", new String[]{"[4,4,6]","[7,7]","[4,4]"});
        addCiPai("永遇乐", "千古江山，英雄无觅，孙仲谋处。舞榭歌台，风流总被，雨打风吹去。斜阳草树，寻常巷陌，人道寄奴曾住。想当年，金戈铁马，气吞万里如虎。\n元嘉草草，封狼居胥，赢得仓皇北顾。四十三年，望中犹记，烽火扬州路。可堪回首，佛狸祠下，一片神鸦社鼓。凭谁问：廉颇老矣，尚能饭否？", new String[]{"[4,4,4]","[4,5]","[4,4,6]","[3,4,6]","[4,4,6]","[4,4,5]","[4,4,6]","[3,4,4]","[4]","[4,4]"});
        addCiPai("一剪梅", "红藕香残玉簟秋。轻解罗裳，独上兰舟。云中谁寄锦书来？雁字回时，月满西楼。\n花自飘零水自流。一种相思，两处闲愁。此情无计可消除，才下眉头，却上心头。", new String[]{"[7]","[4,4]","[7,4,4]","[7,4,4]"});
        addCiPai("西江月", "明月别枝惊鹊，清风半夜鸣蝉。稻花香里说丰年，听取蛙声一片。\n七八个星天外，两三点雨山前。旧时茅店社林边，路转溪桥忽见。", new String[]{"[6,6]","[7,6]","[6]","[7]"});
        addCiPai("浣溪沙", "一曲新词酒一杯，去年天气旧亭台。夕阳西下几时回？\n无可奈何花落去，似曾相识燕归来。小园香径独徘徊。", new String[]{"[7,7]","[7]","[7,7,7]"});
        addCiPai ("渔家傲", "塞下秋来风景异，衡阳雁去无留意。四面边声连角起，千嶂里，长烟落日孤城闭。\n 浊酒一杯家万里，燕然未勒归无计。羌管悠悠霜满地，人不寐，将军白发征夫泪。", new String []{"[7,7]","[7,3,7]","[7]","[7,7,7]","[3,7]"});
        addCiPai ("木兰花", "人生若只如初见，何事秋风悲画扇。等闲变却故人心，却道故人心易变。\n 骊山语罢清宵半，泪雨霖铃终不怨。何如薄幸锦衣郎，比翼连枝当日愿。", new String []{"[7,7]","[7,7,7,7]","[7]","[7,7,7]"});
        addCiPai ("蝶恋花", "花褪残红青杏小，燕子飞时，绿水人家绕。枝上柳绵吹又少。天涯何处无芳草。\n 墙里秋千墙外道，墙外行人，墙里佳人笑。笑渐不闻声渐悄。多情却被无情恼。", new String []{"[7,4,5]","[7,7]","[4,7]","[7]"});
        addCiPai ("武陵春", "风住尘香花已尽，日晚倦梳头。物是人非事事休，欲语泪先流。\n 闻说双溪春尚好，也拟泛轻舟。只恐双溪舴艋舟，载不动许多愁。", new String []{"[7,5]","[7,6]","[7]","[7,5,7,5]","[7,5,7,6]"});
        addCiPai ("如梦令", "昨夜雨疏风骤，浓睡不消残酒。试问卷帘人，却道海棠依旧。知否，知否？应是绿肥红瘦。", new String []{"[6,6]","[5,6]","[7]","[2,2,6]","[6]","[2,2]"});
        addCiPai ("钗头凤", "红酥手，黄縢酒，满城春色宫墙柳。东风恶，欢情薄。一怀愁绪，几年离索。错、错、错。\n 春如旧，人空瘦，泪痕红浥鲛绡透。桃花落，闲池阁。山盟虽在，锦书难托。莫、莫、莫！", new String []{"[3,3,7]","[3,3]","[4,4]","[1,1,1]","[3,3]","[3,3,4,4]"});
        addCiPai ("南乡子", "何处望神州？满眼风光北固楼。千古兴亡多少事？悠悠。不尽长江滚滚流。\n 年少万兜鍪，坐断东南战未休。天下英雄谁敌手？曹刘。生子当如孙仲谋。", new String []{"[5,7]","[7,2]","[7]","[7,2,7]","[5]"});
        addCiPai ("长相思", "山一程，水一程，身向榆关那畔行，夜深千帐灯。\n 风一更，雪一更，聒碎乡心梦不成，故园无此声。", new String []{"[3,3,7]","[5]","[3,3]","[3,3,7,5]","[5]"});
        addCiPai ("醉花阴", "薄雾浓云愁永昼，瑞脑销金兽。佳节又重阳，玉枕纱厨，半夜凉初透。\n 东篱把酒黄昏后，有暗香盈袖。莫道不销魂，帘卷西风，人比黄花瘦。", new String []{"[7,5]","[5,4,5]","[4,5]","[7]","[5]"});
        addCiPai ("浪淘沙令", "帘外雨潺潺，春意阑珊。罗衾不耐五更寒。梦里不知身是客，一晌贪欢。\n 独自莫凭栏，无限江山。别时容易见时难。流水落花春去也，天上人间。", new String []{"[5,4]","[7]","[7,4]","[7]","[4]"});
        addCiPai ("相见欢", "无言独上西楼，月如钩。寂寞梧桐深院锁清秋。\n 剪不断，理还乱，是离愁。别是一般滋味在心头。", new String []{"[6,3]","[9]","[3,3,3]","[3,3,3,9]","[6,3,9]"});
        addCiPai ("清平乐", "茅檐低小，溪上青青草。醉里吴音相媚好，白发谁家翁媪？\n 大儿锄豆溪东，中儿正织鸡笼。最喜小儿亡赖，溪头卧剥莲蓬。", new String []{"[4,5]","[7,6]","[6,6]","[6,6]","[7]"});
        addCiPai ("渔家傲", "天接云涛连晓雾，星河欲转千帆舞。仿佛梦魂归帝所。闻天语，殷勤问我归何处。\n 我报路长嗟日暮，学诗谩有惊人句。九万里风鹏正举。风休住，蓬舟吹取三山去！", new String []{"[7,7]","[7]","[3,7]","[6,6]","[7,7,7]","[3,7]"});
        addCiPai ("望江南", "春未老，风细柳斜斜。试上超然台上看，半壕春水一城花。烟雨暗千家。\n 寒食后，酒醒却咨嗟。休对故人思故国，且将新火试新茶。诗酒趁年华。", new String []{"[3,5]","[7,7,5]","[7,7]","[5]","[7]"});
        addCiPai ("水龙吟", "楚天千里清秋，水随天去秋无际。遥岑远目，献愁供恨，玉簪螺髻。落日楼头，断鸿声里，江南游子。把吴钩看了，栏杆拍遍，无人会，登临意。\n 休说鲈鱼堪脍，尽西风，季鹰归未？求田问舍，怕应羞见，刘郎才气。可惜流年，忧愁风雨，树犹如此！倩何人唤取，红巾翠袖，揾英雄泪！", new String []{"[6,7]","[4,4,4]","[4,4,4]","[5,4,3,3]","[6,3,4]","[4,4,4]","[5,4,4]","[4,4]","[3,3]"});
        addCiPai ("忆江南", "江南好，风景旧曾谙。日出江花红胜火，春来江水绿如蓝。能不忆江南？", new String []{"[3,5]","[7,7,5]","[7,7]","[5]","[7]"});
        addCiPai ("望海潮", "东南形胜，三吴都会，钱塘自古繁华。烟柳画桥，风帘翠幕，参差十万人家。云树绕堤沙，怒涛卷霜雪，天堑无涯。市列珠玑，户盈罗绮，竞豪奢。\n 重湖叠𪩘清嘉，有三秋桂子，十里荷花。羌管弄晴，菱歌泛夜，嬉嬉钓叟莲娃。千骑拥高牙，乘醉听箫鼓，吟赏烟霞。异日图将好景，归去凤池夸。", new String []{"[4,4,6]","[4,4]","[5,5,4]","[4,4,3]","[6,5,4]","[4,4,6]","[5,5,4]","[6,5]"});
        addCiPai ("扬州慢", "淮左名都，竹西佳处，解鞍少驻初程。过春风十里，尽荠麦青青。自胡马窥江去后，废池乔木，犹厌言兵。渐黄昏，清角吹寒，都在空城。\n 杜郎俊赏，算而今，重到须惊。纵豆蔻词工，青楼梦好，难赋深情。二十四桥仍在，波心荡，冷月无声。念桥边红药，年年知为谁生？", new String []{"[4,4,6]","[5,5]","[7,4,4]","[3,4,4]","[4,3,4]","[5,4,4]","[6,3,4]","[5,6]","[6]","[3,4]"});
        addCiPai ("八声甘州", "对潇潇暮雨洒江天，一番洗清秋。渐霜风凄紧，关河冷落，残照当楼。是处红衰翠减，苒苒物华休。唯有长江水，无语东流。\n 不忍登高临远，望故乡渺邈，归思难收。叹年来踪迹，何事苦淹留。想佳人妆楼颙望，误几回、天际识归舟。争知我，倚阑杆处，正恁凝愁！", new String []{"[8,5]","[5,4,4]","[6,5]","[5,4]","[6,5,4]","[5,5]","[7,3,5]","[3,4,4]","[8]","[4,4]"});
        addCiPai ("诉衷情", "当年万里觅封侯，匹马戍梁州。关河梦断何处？尘暗旧貂裘。\n 胡未灭，鬓先秋，泪空流。此生谁料，心在天山，身老沧洲。", new String []{"[7,5]","[6,5]","[3,3,3]","[4,4,4]"});
        addCiPai ("摸鱼儿", "问世间，情是何物，直教生死相许？天南地北双飞客，老翅几回寒暑。欢乐趣，离别苦，就中更有痴儿女。君应有语：渺万里层云，千山暮雪，只影向谁去？\n 横汾路，寂寞当年箫鼓，荒烟依旧平楚。招魂楚些何嗟及，山鬼自啼风雨。天也妒，未信与，莺儿燕子俱黄土。千秋万古，为留待骚人，狂歌痛饮，来访雁丘处。", new String []{"[3,3,6]","[7,6]","[3,3,7]","[4,5,4,5]","[3,6,6]","[7,6]","[3,3,7]","[4,5,4,5]","[4,5]"});
        addCiPai ("菩萨蛮", "人人尽说江南好，游人只合江南老。春水碧于天，画船听雨眠。\n 垒边人似月，皓腕凝霜雪。未老莫还乡，还乡须断肠。", new String []{"[7,7]","[5,5]","[7]"});
        addCiPai ("忆秦娥", "箫声咽，秦娥梦断秦楼月。秦楼月，年年柳色，灞陵伤别。\n 乐游原上清秋节，咸阳古道音尘绝。音尘绝，西风残照，汉家陵阙。", new String []{"[3,7]","[3,4,4]","[7,7]","[3,4,4]","[4,4]","[3]"});
        addCiPai ("浪淘沙", "把酒祝东风，且共从容。垂杨紫陌洛城东。总是当时携手处，游遍芳丛。\n 聚散苦匆匆，此恨无穷。今年花胜去年红。可惜明年花更好，知与谁同？", new String []{"[5,4]","[7,7,4]","[5,4]","[7]","[7,4]","[5,4,7]"});
        addCiPai ("鹧鸪天", "彩袖殷勤捧玉钟。当年拚却醉颜红。舞低杨柳楼心月，歌尽桃花扇底风。\n 从别后，忆相逢。几回魂梦与君同。今宵剩把银釭照，犹恐相逢是梦中。", new String []{"[7,7]","[3,3,7]","[3,3]","[7]","[7,7,7,7]"});
        addCiPai ("玉楼春", "尊前拟把归期说，欲语春容先惨咽。人生自是有情痴，此恨不关风与月。\n 离歌且莫翻新阕，一曲能教肠寸结。直须看尽洛城花，始共春风容易别。", new String []{"[7,7]","[7]","[7,7,7,7]"});
        addCiPai ("唐多令", "芦叶满汀洲，寒沙带浅流。二十年重过南楼。柳下系船犹未稳，能几日，又中秋。\n 黄鹤断矶头，故人今在否？旧江山浑是新愁。欲买桂花同载酒，终不似，少年游。", new String []{"[5,5]","[7]","[7,3,3]","[7,7,3,3]"});
        addCiPai ("苏幕遮", "燎沉香，消溽暑。鸟雀呼晴，侵晓窥檐语。叶上初阳干宿雨，水面清圆，一一风荷举。\n 故乡遥，何日去？家住吴门，久作长安旅。五月渔郎相忆否？小楫轻舟，梦入芙蓉浦。", new String []{"[3,3]","[4,5]","[7,4,5]","[3,3,4,5]","[7]","[4,5]","[4,4]"});
        addCiPai ("贺新郎", "甚矣吾衰矣。怅平生、交游零落，只今余几！白发空垂三千丈，一笑人间万事。问何物、能令公喜？我见青山多妩媚，料青山见我应如是。情与貌，略相似。\n 一尊搔首东窗里。想渊明、停云诗就，此时风味。江左沉酣求名者，岂识浊醪妙理。回首叫、云飞风起。不恨古人吾不见，恨古人不见吾狂耳。知我者，二三子。", new String []{"[5]","[3,4,4]","[7,6]","[3,4]","[7,8]","[3,3]","[7]","[3,4,4]","[7,6]","[3,4]","[7,8]","[3,3]"});
        addCiPai ("点绛唇", "蹴罢秋千，起来慵整纤纤手。露浓花瘦，薄汗轻衣透。\n 见客入来，袜刬金钗溜。和羞走，倚门回首，却把青梅嗅。", new String []{"[4,7]","[4,5]","[4,5]","[3,4,5]"});
        addCiPai ("踏莎行", "雾失楼台，月迷津渡。桃源望断无寻处。可堪孤馆闭春寒，杜鹃声里斜阳暮。\n 驿寄梅花，鱼传尺素。砌成此恨无重数。郴江幸自绕郴山，为谁流下潇湘去。", new String []{"[4,4]","[7]","[7,7]","[4,4,7]","[7,7,7]"});
        addCiPai ("桂枝香", "登临送目，正故国晚秋，天气初肃。千里澄江似练，翠峰如簇。归帆去棹残阳里，背西风，酒旗斜矗。彩舟云淡，星河鹭起，画图难足。\n 念往昔，繁华竞逐。叹门外楼头，悲恨相续。千古凭高对此，谩嗟荣辱。六朝旧事随流水，但寒烟衰草凝绿。至今商女，时时犹唱，后庭遗曲。", new String []{"[4,5,4]","[7,4]","[7,3,4]","[4,4,4]","[3,4]","[5,4]","[6,4]","[7,7]","[4,4,4]","[4,5]"});
        addCiPai ("行香子", "清夜无尘，月色如银。酒斟时、须满十分。浮名浮利，虚苦劳神。叹隙中驹，石中火，梦中身。\n 虽抱文章，开口谁亲。且陶陶、乐尽天真。几时归去，作个闲人。对一张琴，一壶酒，一溪云。", new String []{"[4,4]","[3,4]","[4,4]","[4,3,3]","[4,4]","[3,4]","[4,4]","[4,3,3]"});
        addCiPai ("太常引", "一轮秋影转金波，飞镜又重磨。把酒问姮娥：被白发，欺人奈何？\n 乘风好去，长空万里，直下看山河。斫去桂婆娑，人道是，清光更多。", new String []{"[7,5]","[5,3,4]","[4,4,5]","[5,3,4]"});
        addCiPai ("鹤冲天", "黄金榜上，偶失龙头望。明代暂遗贤，如何向。未遂风云便，争不恣狂荡。何须论得丧？才子词人，自是白衣卿相。\n 烟花巷陌，依约丹青屏障。幸有意中人，堪寻访。且恁偎红倚翠，风流事、平生畅。青春都一饷。忍把浮名，换了浅斟低唱！", new String []{"[4,5]","[5,3]","[5,5]","[5,5]","[5]","[4,6]","[4,6]","[5,3]","[6,3,3]","[5,4,6]"});
        addCiPai ("采桑子", "轻舟短棹西湖好，绿水逶迤。芳草长堤，隐隐笙歌处处随。\n 无风水面琉璃滑，不觉船移。微动涟漪，惊起沙禽掠岸飞。", new String []{"[7,4]","[4,7]","[7,4]","[4,7]"});
        addCiPai ("乌夜啼", "昨夜风兼雨，帘帏飒飒秋声。烛残漏断频欹枕，起坐不能平。\n 世事漫随流水，算来一梦浮生。醉乡路稳宜频到，此外不堪行。", new String []{"[5,6]","[7,5]","[6,6]","[7,5]"});
        addCiPai ("满庭芳", "山抹微云，天连衰草，画角声断谯门。暂停征棹，聊共引离尊。多少蓬莱旧事，空回首、烟霭纷纷。斜阳外，寒鸦万点，流水绕孤村。\n 销魂当此际，香囊暗解，罗带轻分。谩赢得、青楼薄幸名存。此去何时见也？襟袖上、空惹啼痕。伤情处，高城望断，灯火已黄昏。", new String []{"[4,4,6]","[4,5]","[6,3,4]","[3,4,5]","[5,4,4]","[3,6]","[6,3,4]","[3,4,5]","[3,4,5]","[5,4,4]","[3,6,6]","[3,4]","[3,4,5]"});
        addCiPai ("天仙子", "水调数声持酒听，午醉醒来愁未醒。送春春去几时回？临晚镜，伤流景，往事后期空记省。\n 沙上并禽池上瞑，云破月来花弄影。重重帘幕密遮灯，风不定，人初静，明日落红应满径。", new String []{"[7,7]","[7]","[3,3,7]","[7,7,7]","[3,3]"});
        addCiPai ("六州歌头", "少年侠气，交结五都雄。肝胆洞，毛发耸。立谈中，死生同。一诺千金重。推翘勇，矜豪纵。轻盖拥，联飞鞚，斗城东。轰饮酒垆，春色浮寒瓮，吸海垂虹。闲呼鹰嗾犬，白羽摘雕弓，狡穴俄空。乐匆匆。\\n 似黄粱梦，辞丹凤；明月共，漾孤篷。官冗從，怀倥偬；落尘笼，簿书丛。鶡弁如云众，供粗用，忽奇功。笳鼓动，渔阳弄，思悲翁。不请长缨，系取天骄种，剑吼西风。恨登山临水，手寄七弦桐，目送归鸿。", new String []{"[4,5]","[3,3]","[3,3,3,3]","[3,3,5]","[3,3]","[3,3,3]","[4,5,4]","[5,5,4]","[5,5,4,3]","[4,3]","[3,3,3,3,3,3]","[5,3,3]","[3,3,3]","[4,5,4]","[5,5,4]"});
        addCiPai ("少年游", "长安古道马迟迟，高柳乱蝉嘶。夕阳岛外，秋风原上，目断四天垂。\n 归云一去无踪迹，何处是前期？狎兴生疏，酒徒萧索，不似少年时。", new String []{"[7,5]","[4,4,5]","[4,5]"});
        addCiPai ("千秋岁", "数声鶗鴂，又报芳菲歇。惜春更把残红折。雨轻风色暴，梅子青时节。永丰柳，无人尽日飞花雪。\n 莫把幺弦拨，怨极弦能说。天不老，情难绝。心似双丝网，中有千千结。夜过也，东窗未白凝残月。", new String []{"[4,5]","[7]","[5,5]","[3,7]","[5,5]","[3,3]","[5,5]","[3,5]"});
        addCiPai ("谒金门", "风乍起，吹皱一池春水。闲引鸳鸯香径里，手挼红杏蕊。\n 斗鸭阑干独倚，碧玉搔头斜坠。终日望君君不至，举头闻鹊喜。", new String []{"[3,5]","[7,5]","[6,6]","[7,5]"});
        addCiPai ("南乡子", "何处望神州？满眼风光北固楼。千古兴亡多少事？悠悠。不尽长江滚滚流。\n 年少万兜鍪，坐断东南战未休。天下英雄谁敌手？曹刘。生子当如孙仲谋。", new String []{"[5,7]","[7]","[2,7]","[7,2]"});
        addCiPai ("摊破浣溪沙", "菡萏香销翠叶残，西风愁起绿波间。还与韶光共憔悴，不堪看。\n 细雨梦回鸡塞远，小楼吹彻玉笙寒。多少泪珠何限恨，倚阑干。", new String []{"[7,7]","[7,3]"});
        addCiPai ("御街行", "纷纷坠叶飘香砌，夜寂静，寒声碎。真珠帘卷玉楼空，天淡银河垂地。年年今夜，月华如练，长是人千里。\n 愁肠已断无由醉，酒未到，先成泪。残灯明灭枕头欹，谙尽孤眠滋味。都来此事，眉间心上，无计相回避。", new String []{"[7,3,3]","[7,6]","[4,4,7]","[3,3]","[7,6]","[4,4,5]"});
        addCiPai ("凤凰台上忆吹箫", "香冷金猊，被翻红浪，起来慵自梳头。任宝奁尘满，日上帘钩。生怕离怀别苦，多少事、欲说还休。新来瘦，非干病酒，不是悲秋。\n 休休，这回去也，千万遍《阳关》，也则难留。念武陵人远，烟锁秦楼。惟有楼前流水，应念我、终日凝眸。凝眸处，从今又添，一段新愁。", new String []{"[4,4,6]","[5,4]","[6,3,4]","[3,4,4]","[2,4,5,4]","[5,4]","[6,3,4]","[3,4,4]"});
        addCiPai ("兰陵王", "柳阴直，烟里丝丝弄碧。隋堤上、曾见几番，拂水飘绵送行色。登临望故国，谁识京华倦客？长亭路，年去岁来，应折柔条过千尺。\n 闲寻旧踪迹，又酒趁哀弦，灯照离席。梨花榆火催寒食。愁一箭风快，半篙波暖，回头迢递便数驿，望人在天北。\n 凄恻，恨堆积！渐别浦萦回，津堠岑寂，斜阳冉冉春无极。念月榭携手，露桥闻笛。沉思前事，似梦里，泪暗滴。", new String []{"[3,6]","[3,4,7]","[5,6]","[3,4,7]","[5,5,4]","[7]","[5,4,7,5]","[2,3]","[5,4,7]","[5,4]","[4,3,3]"});
        addCiPai ("玉蝴蝶", "望处雨收云断，凭阑悄悄，目送秋光。晚景萧疏，堪动宋玉悲凉。水风轻，蘋花渐老，月露冷、梧叶飘黄。遣情伤。故人何在，烟水茫茫。\n 难忘，文期酒会，几孤风月，屡变星霜。海阔山遥，未知何处是潇湘。念双燕、难凭远信，指暮天、空识归航。黯相望。断鸿声里，立尽斜阳。", new String []{"[6,4,4]","[4,6]","[3,4,3,4]","[3,4,4]","[4,4]","[2,4,4,4]","[4,7]","[3,4,3,4]","[3,4,4]"});
        addCiPai ("生查子", "去年元夜时，花市灯如昼。\n 月上柳梢头，人约黄昏后。\n 今年元夜时，月与灯依旧。\n 不见去年人，泪湿春衫袖。", new String []{"[5,5]"});
        addCiPai ("阮郎归", "绿槐高柳咽新蝉。薰风初入弦。碧纱窗下水沉烟。棋声惊昼眠。\n 微雨过，小荷翻。榴花开欲然。玉盆纤手弄清泉。琼珠碎却圆。", new String []{"[7,5]","[7,5]","[3,3,5]","[7,5]"});
        addCiPai ("暗香", "旧时月色，算几番照我，梅边吹笛？唤起玉人，不管清寒与攀摘。何逊而今渐老，都忘却春风词笔。但怪得竹外疏花，香冷入瑶席。\n 江国，正寂寂，叹寄与路遥，夜雪初积。翠尊易泣，红萼无言耿相忆。长记曾携手处，千树压、西湖寒碧。又片片、吹尽也，几时见得？", new String []{"[4,5,4]","[4,7]","[6,7]","[7,5]","[2,3,5,4]","[4,7]","[7,3,4]","[3,4,4]"});
        addCiPai ("鹤冲天", "黄金榜上，偶失龙头望。明代暂遗贤，如何向。未遂风云便，争不恣狂荡。何须论得丧？才子词人，自是白衣卿相。\n 烟花巷陌，依约丹青屏障。幸有意中人，堪寻访。且恁偎红倚翠，风流事、平生畅。青春都一饷。忍把浮名，换了浅斟低唱！", new String []{"[4,5]","[5,3]","[5,5,5]","[4,6]","[4,6]","[5,3]","[6,3,3]","[5,4,6]"});
        addCiPai ("天仙子", "水调数声持酒听，午醉醒来愁未醒。送春春去几时回？临晚镜，伤流景，往事后期空记省。\n 沙上并禽池上瞑，云破月来花弄影。重重帘幕密遮灯，风不定，人初静，明日落红应满径。", new String []{"[7,7]","[7]","[3,3,7]","[7,7,7]"});
        addCiPai ("七律", "风急天高猿啸哀， 渚清沙白鸟飞回。无边落木萧萧下， 不尽长江滚滚来。万里悲秋常作客，百年多病独登台。艰难苦恨繁霜鬓， 潦倒新停浊酒杯。", new String []{"[7,7]","[7]"});
        addCiPai ("五律", "国破山河在，城春草木深。\n 感时花溅泪，恨别鸟惊心。\n 烽火连三月，家书抵万金。\n 白头搔更短，浑欲不胜簪。", new String []{"[5,5]","[5]"});
        addCiPai ("五言绝句", "好雨知时节，当春乃发生。随风潜入夜，润物细无声。野径云俱黑，江船火独明。晓看红湿处，花重锦官城。", new String []{"[5,5]","[5]"});
        addCiPai ("七言绝句", "秦时明月汉时关，万里长征人未还。\n 但使龙城飞将在，不教胡马度阴山。", new String []{"[7,7]","[7]"});

    }

    private void addCiPai(String name, String exampleText, String[] sentenceLengths) {
        CiPai cipai = new CiPai();
        cipai.setId((long) (ciPais.size() + 1));
        cipai.setName(name);
        cipai.setExampleText(exampleText);
        cipai.setSentenceLengths(sentenceLengths);
        ciPais.add(cipai);
    }

    private void initializeRhymeData() {
        // 清空现有数据
        rhymeGroups.clear();

        // 初始化押韵数据
        addRhymeGroup("(ong,iong)", "[\"中\",\"松\",\"空\",\"功\",\"通\",\"钟\",\"终\",\"从\",\"丛\",\"同\",\"绒\",\"童\",\"龙\",\"红\",\"重\",\"动\",\"涌\",\"东\",\"冻\",\"攻\",\"冬\",\"浓\",\"胸\",\"穷\",\"雄\",\"弄\",\"送\",\"颂\",\"痛\",\"众\",\"宠\",\"懂\",\"孔\",\"匆\",\"拥\",\"咏\",\"工\",\"荣\",\"翁\",\"弓\",\"众\", \"冢\", \"踵\", \"栋\", \"洞\", \"箜\", \"孔\", \"鸿\", \"鸿\", \"虹\", \"泓\", \"炯\", \"穹\", \"茕\", \"琼\", \"永\", \"咏\", \"庸\", \"用\", \"拥\"]");
        addRhymeGroup("(en,eng)", "[\"风\",\"梦\",\"朋\",\"生\",\"声\",\"灯\",\"城\",\"成\",\"程\",\"能\",\"胜\",\"腾\",\"冷\",\"盛\",\"横\",\"绳\",\"登\",\"增\",\"征\",\"争\",\"层\",\"赠\",\"逢\",\"奉\",\"捧\",\"猛\",\"春\",\"深\",\"人\",\"门\",\"分\",\"身\",\"本\",\"粉\",\"针\",\"恨\",\"神\",\"温\",\"痕\",\"们\",\"盆\",\"仁\",\"沉\",\"尘\",\"陈\",\"臣\",\"枕\",\"诊\",\"狠\",\"肯\",\"忍\",\"辰\",\"晨\",\"认\",\"慎\",\"镇\",\"振\",\"恩\",\"奔\",\"纷\",\"根\",\"真\",\"珍\",\"任\",\"阵\",\"问\",\"稳\",\"吻\",\"滚\",\"盾\",\"困\",\"顺\",\"润\",\"闻\",\"文\"]");
        addRhymeGroup("(in,ing)", "[\"情\",\"性\",\"名\",\"兵\",\"星\",\"明\",\"病\",\"定\",\"镜\",\"行\",\"青\",\"兴\",\"轻\",\"鸣\",\"亭\",\"惊\",\"英\",\"鹰\",\"灵\",\"敬\",\"晴\",\"停\",\"京\",\"睛\",\"领\",\"岭\",\"请\",\"艇\",\"醒\",\"映\",\"庆\",\"幸\",\"晶\",\"应\",\"境\",\"静\",\"听\",\"厅\",\"心\",\"品\",\"进\",\"亲\",\"新\",\"音\",\"今\",\"金\",\"林\",\"劲\",\"尽\",\"信\",\"民\",\"勤\",\"琴\",\"银\",\"邻\",\"临\",\"引\",\"饮\",\"紧\",\"锦\",\"因\",\"阴\",\"荫\",\"宾\",\"襟\",\"津\",\"巾\",\"薪\",\"寝\",\"印\",\"鬓\",\"近\",\"吟\",\"您\",\"贫\",\"聘\",\"敏\"]");
        addRhymeGroup("(a,ia,Ua)", "[\"华\",\"家\",\"发\",\"化\",\"话\",\"画\",\"马\",\"法\",\"打\",\"花\",\"茶\",\"价\",\"下\",\"沙\",\"杀\",\"纱\",\"麻\",\"达\",\"察\",\"塔\",\"瓜\",\"卡\",\"骂\",\"霸\",\"洒\",\"霞\",\"假\",\"雅\",\"牙\",\"芽\",\"怕\",\"娃\",\"鸭\",\"夏\",\"啊\",\"他\",\"厦\",\"差\",\"拉\",\"妈\",\"答\",\"靶\",\"挂\",\"瘩\",\"疤\",\"嫁\",\"拿\",\"吗\",\"爸\",\"峡\",\"侠\",\"佳\",\"夹\",\"涯\",\"吧\",\"嘛\",\"垮\",\"夸\",\"帕\",\"踏\",\"丫\",\"洼\",\"筏\",\"轧\",\"啥\",\"俩\",\"砸\",\"呀\",\"她\"]");
        addRhymeGroup("(un)", "[\"文\",\"论\",\"闻\",\"村\",\"损\",\"笋\",\"准\",\"轮\",\"伦\",\"昏\",\"婚\",\"孙\",\"存\",\"唇\",\"魂\",\"棍\",\"吞\",\"顿\",\"盾\",\"滚\",\"困\",\"顺\",\"润\",\"尊\",\"谆\",\"纯\",\"吮\",\"昆\",\"仑\",\"馄\",\"饨\",\"臀\",\"准\", \"醇\", \"春\", \"莼\", \"瞬\", \"舜\", \"遁\", \"囵\", \"坤\", \"鲲\",  \"混\", \"昏\", \"魂\", \"浑\", \"樽\", \"村\", \"寸\", \"存\"]");
        addRhymeGroup("(ün)", "[\"云\",\"军\",\"寻\",\"君\",\"群\",\"裙\",\"训\",\"允\",\"俊\",\"峻\",\"勋\",\"讯\",\"韵\",\"运\",\"晕\",\"均\",\"钧\",\"筠\",\"运\",\"韵\",\"吮\"]");
        addRhymeGroup("(o,uo)", "[\"多\",\"火\",\"国\",\"过\",\"落\",\"果\",\"波\",\"坡\",\"所\",\"作\",\"拙\",\"浊\",\"酌\",\"驮\",\"搓\",\"朵\",\"躲\",\"诺\",\"烁\",\"脱\",\"桌\",\"夺\",\"锣\",\"阔\",\"获\",\"惑\",\"托\",\"错\",\"索\",\"搓\",\"妥\",\"握\",\"措\",\"漠\",\"寞\",\"摩\",\"魔\",\"磨\",\"搏\",\"博\",\"泊\",\"魄\",\"播\",\"迫\",\"拨\",\"剥\",\"我\",\"窝\",\"着\",\"螺\",\"拙\",\"浊\",\"酌\",\"驮\",\"搓\",\"朵\",\"躲\",\"诺\",\"烁\",\"脱\",\"桌\",\"夺\",\"锣\",\"阔\",\"获\",\"惑\",\"托\",\"错\",\"索\",\"搓\",\"妥\",\"握\",\"措\",\"漠\",\"寞\",\"波\",\"坡\",\"摩\",\"魔\",\"磨\",\"搏\",\"博\",\"泊\",\"魄\",\"播\",\"迫\",\"拨\",\"剥\",\"我\",\"窝\",\"着\",\"螺\"]");
        addRhymeGroup("(e)", "[\"河\",\"车\",\"客\",\"色\",\"课\",\"热\",\"德\",\"和\",\"合\",\"刻\",\"乐\",\"策\",\"册\",\"测\",\"设\",\"革\",\"格\",\"隔\",\"阁\",\"何\",\"壳\",\"哥\",\"戈\",\"鸽\",\"喝\",\"渴\",\"舍\",\"恶\",\"鹤\",\"泽\",\"择\",\"则\",\"责\",\"辙\",\"扯\",\"奢\",\"娥\",\"涉\",\"特\",\"瑟\",\"涩\"]");
        addRhymeGroup("(i)", "[\"奇\",\"击\",\"笔\",\"底\",\"米\",\"礼\",\"体\",\"里\",\"鸡\",\"级\",\"计\",\"济\",\"极\",\"机\",\"及\",\"集\",\"衣\",\"旗\",\"席\",\"力\",\"地\",\"戏\",\"弟\",\"系\",\"利\",\"意\",\"义\",\"气\",\"立\",\"敌\",\"你\",\"起\",\"技\",\"期\",\"记\",\"妻\",\"已\",\"理\",\"议\",\"异\",\"艺\",\"丽\",\"迷\",\"泥\",\"稀\",\"息\",\"移\",\"碧\",\"益\",\"翼\",\"逸\",\"役\",\"历\",\"密\",\"易\",\"挤\",\"姬\",\"弃\",\"泣\",\"帝\",\"季\",\"寂\",\"替\",\"细\",\"西\",\"喜\",\"绩\",\"激\",\"批\",\"宜\",\"怡\",\"疑\",\"笛\",\"比\",\"迹\",\"洗\",\"以\",\"际\",\"溪\",\"戚\",\"逼\",\"低\",\"堤\",\"基\",\"滴\",\"依\",\"医\",\"提\",\"啼\",\"习\",\"励\",\"吸\",\"悉\",\"昔\",\"避\",\"毕\",\"溢\",\"译\",\"题\",\"事\",\"市\",\"子\",\"字\",\"实\",\"石\",\"食\",\"识\",\"止\",\"室\",\"日\",\"制\",\"势\",\"是\",\"世\",\"士\",\"试\",\"枝\",\"知\",\"汁\",\"吃\",\"诗\",\"师\",\"失\",\"纸\",\"齿\",\"史\",\"志\",\"致\",\"思\",\"丝\",\"词\",\"死\",\"职\",\"迟\",\"池\",\"时\",\"视\",\"质\",\"逝\",\"资\",\"私\",\"辞\",\"持\",\"址\",\"释\",\"誓\",\"始\",\"滋\",\"姿\"]");
        addRhymeGroup("(ü)", "[\"雨\",\"曲\",\"区\",\"鱼\",\"女\",\"去\",\"语\",\"玉\",\"句\",\"局\",\"誉\",\"欲\",\"屈\",\"菊\",\"域\",\"絮\",\"续\",\"遇\",\"浴\",\"举\",\"旅\",\"缕\",\"侣\",\"取\",\"绿\",\"律\",\"趣\",\"与\", \"余\", \"羽\", \"郁\", \"隅\", \"吕\", \"侣\", \"履\", \"闾\",  \"驹\", \"巨\", \"掬\", \"矩\", \"聚\", \"俱\", \"橘\", \"菊\", \"炬\", \"惧\", \"屈\", \"驱\", \"躯\", \"须\", \"徐\", \"绪\", \"许\", \"序\", \"叙\"]");
        addRhymeGroup("(ai,uai)", "[\"才\",\"爱\",\"外\",\"台\",\"彩\",\"白\",\"来\",\"赛\",\"派\",\"脉\",\"海\",\"害\",\"戴\",\"盖\",\"材\",\"牌\",\"塞\",\"态\",\"在\",\"载\",\"菜\",\"代\",\"怀\",\"坏\",\"快\",\"帅\",\"排\",\"开\",\"拍\",\"采\",\"睬\",\"猜\",\"债\",\"寨\",\"栽\",\"摘\", \"钗\", \"差\", \"柴\", \"筛\", \"百\", \"拜\", \"柏\", \"败\", \"湃\", \"麦\", \"埋\", \"迈\", \"待\", \"代\", \"黛\", \"歹\", \"岱\", \"太\", \"泰\", \"苔\", \"耐\", \"奈\", \"籁\", \"改\", \"楷\", \"铠\", \"揩\", \"再\", \"哉\", \"灾\", \"栽\", \"猜\", \"材\", \"财\", \"彩\", \"歪\"]");
        addRhymeGroup("(iang,uang)", "[\"光\",\"江\",\"枪\",\"香\",\"望\",\"阳\",\"厂\",\"党\",\"方\",\"上\",\"想\",\"长\",\"场\",\"章\",\"向\",\"堂\",\"唱\",\"房\",\"王\",\"装\",\"霜\",\"亡\",\"床\",\"掌\",\"赏\",\"网\",\"往\",\"像\",\"巷\",\"窗\",\"样\",\"亮\",\"伤\",\"汤\",\"张\",\"邦\",\"苍\",\"荡\",\"放\",\"浪\",\"肠\",\"航\",\"行\",\"郎\",\"忙\",\"茫\",\"爽\",\"忘\",\"壮\",\"相\",\"乡\",\"扬\",\"杨\",\"洋\",\"芳\",\"养\",\"奖\",\"响\",\"良\",\"粮\",\"凉\",\"量\",\"娘\",\"强\",\"妆\",\"庄\",\"藏\",\"常\",\"昂\",\"旁\",\"爱\",\"谎\",\"况\",\"撞\",\"访\",\"朗\",\"舱\",\"冈\",\"扛\",\"康\",\"丈\",\"仗\",\"仰\",\"讲\",\"桨\"]");
        addRhymeGroup("(ei,ui,uei)", "[\"伟\",\"会\",\"飞\",\"威\",\"回\",\"岁\",\"费\",\"备\",\"归\",\"规\",\"泪\",\"妹\",\"内\",\"眉\",\"醉\",\"罪\",\"退\",\"美\",\"雷\",\"队\",\"水\",\"谁\",\"对\",\"贵\",\"碑\",\"悲\",\"杯\",\"非\",\"危\",\"微\",\"吹\",\"北\",\"累\",\"辈\",\"为\",\"梅\",\"媒\",\"嘴\",\"位\",\"味\",\"随\",\"辉\",\"媚\",\"倍\",\"徽\",\"慰\",\"鬼\"]");
        addRhymeGroup("(ie,üe)", "[\"月\",\"雪\",\"界\",\"学\",\"夜\",\"节\",\"叶\",\"血\",\"解\",\"写\",\"结\",\"别\",\"约\",\"绝\",\"谢\",\"切\",\"贴\",\"野\",\"接\",\"烈\",\"姐\",\"蝶\",\"戒\",\"捷\",\"洁\",\"杰\",\"跃\",\"悦\",\"撇\", \"瞥\", \"灭\", \"篾\", \"叠\", \"跌\", \"迭\", \"铁\", \"捏\", \"涅\", \"孽\", \"烈\",\"裂\", \"猎\", \"劣\", \"冽\", \"街\",  \"借\", \"截\", \"阶\", \"杰\", \"结\", \"劫\", \"洁\", \"芥\", \"且\", \"切\", \"怯\", \"谐\", \"协\", \"写\", \"斜\",\"血\", \"邪\", \"携\", \"歇\", \"泻\", \"野\", \"页\", \"业\"]");
        addRhymeGroup("(an,ian,uan,üan )", "[\"线\",\"面\",\"眼\",\"点\",\"演\",\"谈\",\"板\",\"天\",\"团\",\"年\",\"前\",\"钱\",\"见\",\"言\",\"山\",\"变\",\"电\",\"权\",\"员\",\"缘\",\"全\",\"怨\",\"愿\",\"泉\",\"远\",\"选\",\"战\",\"站\",\"片\",\"淡\",\"饭\",\"难\",\"然\",\"短\",\"管\",\"暖\",\"关\",\"观\",\"传\",\"船\",\"还\",\"办\",\"岸\",\"半\",\"间\",\"肩\",\"安\",\"班\",\"干\",\"园\",\"圆\",\"源\",\"看\",\"脸\",\"箭\",\"恋\",\"田\",\"闲\",\"弦\",\"边\",\"算\",\"烟\",\"燕\",\"产\",\"胆\",\"返\",\"感\",\"烦\",\"凡\",\"寒\",\"骗\",\"兰\",\"衫\",\"滩\",\"摊\",\"残\",\"浅\",\"险\",\"念\",\"现\",\"冤\",\"宴\",\"雁\",\"艳\",\"便\",\"范\",\"汉\",\"烂\",\"慢\",\"盼\",\"善\",\"欢\",\"宽\",\"酸\",\"玩\",\"染\",\"散\",\"展\",\"颜\",\"连\",\"莲\",\"换\",\"乱\",\"转\",\"断\",\"单\",\"丹\",\"帆\",\"件\",\"剑\",\"晚\",\"沿\",\"遍\",\"弯\",\"篇\",\"迁\",\"验\",\"漫\",\"倦\",\"劝\",\"悬\",\"旋\",\"颤\",\"盘\",\"帘\",\"廉\",\"绵\",\"辨\",\"辩\",\"担\",\"串\",\"患\",\"畔\",\"川\",\"穿\",\"端\",\"扮\",\"灿\",\"原\",\"尖\",\"炼\",\"赶\",\"敢\",\"喊\",\"坎\"]");
        addRhymeGroup("(ao,iao)", "[\"笑\",\"调\",\"表\",\"角\",\"脚\",\"掉\",\"校\",\"教\",\"料\",\"道\",\"号\",\"报\",\"草\",\"宝\",\"倒\",\"岛\",\"桥\",\"条\",\"药\",\"了\",\"鸟\",\"巧\",\"小\",\"好\",\"老\",\"脑\",\"操\",\"刀\",\"高\",\"帽\",\"闹\",\"套\",\"造\",\"照\",\"票\",\"跳\",\"效\",\"劳\",\"摇\",\"叫\",\"靠\",\"貌\",\"早\",\"枣\",\"找\",\"抱\",\"到\",\"导\",\"宵\",\"谣\",\"跑\",\"绕\",\"扰\",\"少\",\"胞\",\"飘\",\"挑\",\"骚\",\"涛\",\"招\",\"桥\", \"翘\",  \"俏\", \"鞘\", \"彪\", \"窕\",  \"叫\", \"小\", \"晓\", \"潇\", \"销\", \"绡\", \"宵萧\", \"霄\", \"箫\", \"消\", \"鸮\", \"骁\", \"嚣\", \"朝\", \"昭\", \"棹\",  \"潮\",  \"韶\", \"报\", \"孢\", \"抛\", \"袍\", \"锚\", \"矛\", \"旄\", \"到\", \"稻\",  \"韬\", \"恼\",  \"蒿\", \"早\", \"藻\", \"草\", \"扫\", \"耀\", \"妖\", \"遥\"]");
        addRhymeGroup("(u)", "[\"主\",\"务\",\"悟\",\"处\",\"竹\",\"烛\",\"舞\",\"途\",\"图\",\"足\",\"出\",\"树\",\"数\",\"术\",\"木\",\"目\",\"怒\",\"入\",\"书\",\"骨\",\"虎\",\"苦\",\"物\",\"步\",\"度\",\"渡\",\"富\",\"户\",\"复\",\"故\",\"顾\",\"除\",\"土\",\"鼓\",\"服\",\"伏\",\"湖\",\"助\",\"住\",\"注\",\"著\",\"录\",\"幕\",\"毒\",\"读\",\"福\",\"俗\",\"呼\",\"乎\",\"哭\",\"枯\",\"奴\",\"如\",\"祝\",\"雾\",\"楚\",\"睹\",\"斧\",\"珠\",\"无\",\"促\",\"逐\",\"伍\",\"午\",\"独\",\"浮\",\"拂\",\"糊\",\"涂\",\"卒\",\"陆\",\"绿\",\"碌\",\"暮\",\"墓\",\"慕\",\"污\",\"屋\",\"粗\",\"初\",\"肤\",\"暑\",\"诉\",\"宿\",\"腑\",\"抚\",\"述\",\"束\",\"疏\",\"输\",\"舒\",\"谷\",\"肚\",\"赴\",\"付\",\"酷\",\"负\",\"腹\",\"固\"]");
        addRhymeGroup("(iu,ou )", "[\"秋\",\"流\",\"友\",\"球\",\"牛\",\"手\",\"口\",\"斗\",\"头\",\"酒\",\"楼\",\"仇\",\"柳\",\"愁\",\"肉\",\"厚\",\"后\",\"忧\",\"稠\",\"右\",\"有\",\"油\",\"休\",\"游\",\"修\",\"由\",\"留\",\"求\",\"守\",\"首\",\"走\",\"狗\",\"收\",\"州\",\"舟\",\"受\",\"瘦\",\"就\",\"透\",\"日\",\"奏\",\"袖\",\"秀\",\"救\",\"诱\",\"钩\",\"兽\",\"候\",\"周\",\"幽\",\"游\",\"悠\",\"究\",\"羞\",\"久\",\"朽\",\"扣\",\"投\",\"偶\",\"抖\",\"否\",\"柔\",\"凑\",\"够\",\"吼\",\"右\", \"犹\", \"佑\", \"又\", \"友\", \"优\", \"囿\", \"釉\", \"舟\", \"州\", \"昼\", \"皱\", \"骤\",  \"酬\",  \"收\", \"首\", \"守\", \"手\", \"瘦\", \"寿\", \"狩\", \"绶\", \"抔\", \"谋\", \"缪\", \"眸\", \"鍪\", \"缶\",  \"陡\", \"漏\",  \"镂\", \"钩\", \"佝\",  \"侯\", \"逅\", \"就\", \"休\", \"秀\", \"绣\",  \"锈\", \"嗅\", \"走\", \"奏\", \"搜\", \"叟\", \"飕\", \"丢\", \"流\", \"琉\", \"九\", \"丘\", \"裘\"]");
    }

    private void addRhymeGroup(String groupName, String characterList) {
        RhymeGroup group = new RhymeGroup();
        group.setId((long) (rhymeGroups.size() + 1));
        group.setGroupName(groupName);
        group.setCharacterList(characterList);
        rhymeGroups.put(groupName, group);
    }

    /**
     * 将汉字转换为拼音
     * @param chinese 汉字字符串
     * @return 拼音字符串
     */
    private String convertToPinyin(String chinese) {
        StringBuilder pinyinStr = new StringBuilder();
        char[] newChar = chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        try {
            for (int i = 0; i < newChar.length; i++) {
                if (newChar[i] > 128) {
                    String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(newChar[i], defaultFormat);
                    if (pinyinArray != null) {
                        pinyinStr.append(pinyinArray[0]);
                    }
                } else {
                    pinyinStr.append(newChar[i]);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            Log.e(TAG, "Pinyin conversion error", e);
            return null;
        }

        return pinyinStr.toString();
    }

    /**
     * 从拼音中提取韵母
     * @param pinyin 拼音字符串
     * @return 韵母
     */
    private String extractRhymeFinal(String pinyin) {
        // 定义常见的声母
        String[] initials = {"zh", "ch", "sh", "b", "p", "m", "f", "d", "t", "n", "l", "g", "k", "h", "j", "q", "x", "r", "z", "c", "s", "y", "w"};
        
        // 先尝试匹配两个字母的声母
        for (String initial : initials) {
            if (pinyin.startsWith(initial) && pinyin.length() > initial.length()) {
                return pinyin.substring(initial.length());
            }
        }
        
        // 如果没有匹配到两个字母的声母，则尝试匹配单个字母的声母
        if (pinyin.length() > 1) {
            char firstChar = pinyin.charAt(0);
            if (firstChar == 'b' || firstChar == 'p' || firstChar == 'm' || firstChar == 'f' || 
                firstChar == 'd' || firstChar == 't' || firstChar == 'n' || firstChar == 'l' || 
                firstChar == 'g' || firstChar == 'k' || firstChar == 'h' || firstChar == 'j' || 
                firstChar == 'q' || firstChar == 'x' || firstChar == 'r' || firstChar == 'z' || 
                firstChar == 'c' || firstChar == 's' || firstChar == 'y' || firstChar == 'w') {
                return pinyin.substring(1);
            }
        }
        
        // 如果没有声母，则整个拼音都是韵母
        return pinyin;
    }

    // 简化的用户类
    public static class User {
        private Long id;
        private String username;
        private String passwordHash;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPasswordHash() { return passwordHash; }
        public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    }

    // 简化的词牌类
    public static class CiPai {
        private Long id;
        private String name;
        private String exampleText;
        private String[] sentenceLengths;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getExampleText() { return exampleText; }
        public void setExampleText(String exampleText) { this.exampleText = exampleText; }
        public String[] getSentenceLengths() { return sentenceLengths; }
        public void setSentenceLengths(String[] sentenceLengths) { this.sentenceLengths = sentenceLengths; }
    }

    // 简化的押韵组类
    public static class RhymeGroup {
        private Long id;
        private String groupName;
        private String characterList;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        public String getCharacterList() { return characterList; }
        public void setCharacterList(String characterList) { this.characterList = characterList; }
    }

    // 简化的结果类
    public static class Result<T> {
        private int code;
        private String message;
        private T data;

        public static <T> Result<T> success(T data) {
            Result<T> result = new Result<>();
            result.code = 200;
            result.message = "成功";
            result.data = data;
            return result;
        }

        public static <T> Result<T> error(String message) {
            Result<T> result = new Result<>();
            result.code = 500;
            result.message = message;
            return result;
        }

        public int getCode() { return code; }
        public String getMessage() { return message; }
        public T getData() { return data; }
    }
}