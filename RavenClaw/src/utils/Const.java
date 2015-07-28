package utils;

public class Const {
	// D: structure describing a particular forced update
	public static final int FCU_EXPLICIT_CONFIRM =	1;
	public static final int FCU_IMPLICIT_CONFIRM =	2;
	public static final int FCU_UNPLANNED_IMPLICIT_CONFIRM = 3;
	//
	public static final String IET_DIALOG_STATE_CHANGE = "dialog_state_change";
	public static final String IET_USER_UTT_START = "user_utterance_start";
	public static final String IET_USER_UTT_END = "user_utterance_end";
	public static final String IET_PARTIAL_USER_UTT = "partial_user_utterance";
	public static final String IET_SYSTEM_UTT_START = "system_utterance_start";
	public static final String IET_SYSTEM_UTT_END = "system_utterance_end";
	public static final String IET_SYSTEM_UTT_CANCELED = "system_utterance_canceled";
	public static final String IET_FLOOR_OWNER_CHANGES = "floor_owner_changes";
	public static final String IET_SESSION = "session";
	public static final String IET_GUI = "gui";

	
	// D: Topic-initiative: bind concepts only within the current topic / focus
	public static final String WITHIN_TOPIC_ONLY = "bind-this-only";

	// D: Mixed-initiative: bind anything
	public static final String MIXED_INITIATIVE = "bind-anything";
	// D: type describing a concept grounding request
	public static final int GRS_UNPROCESSED= 0;	// the unprocessed status for a grounding request
	public static final int GRS_PENDING    = 1	;	// the pending status for a grounding request
	public static final int GRS_READY      = 2   ;    // the ready status for a grounding request
	public static final int GRS_SCHEDULED  = 3	;	// the scheduled status for a grounding request
	public static final int GRS_EXECUTING  = 5	;	// the executing status for a grounding request
	public static final int GRS_DONE       = 6   ;	// the completed status for a grounding request
	
	// Log tag
	public static final String LAUNCHAPP_TAG = "LaunchApp";
	public static final String DMCORE_STREAM_TAG = "DMCORE";
	public static final String EXPECTATIONAGENDA_STREAM_TAG = "ExpectAgent";
	public static final String CREATE_STREAM_TAG = "CREATE";
	public static final String STATEMANAGER_STREAM_TAG = "StateManager";
	public static final String GROUNDINGMANAGER_STREAM_TAG = "GroundManger";
	public static final String CDTTMANAGER_STREAM = "DttManager";
	public static final String DIALOGTASK_STREAM = "DialogTask";
	public static final String CDATEHYP_TAG = "CDateHyp";
	public static final String CONCEPT_TAG = "CHyp";
	public static final String CSTRING_TAG = "CStringHyp";
	public static final String CSTRUCT_TAG = "CStructConcept";
	public static final String WARNING_STREAM = "InteractionEvent";
	public static final String DIALOGTASK_STREAM_TAG = "DialogTask";
	public static final String INPUTMANAGER_STREAM = "InputManager";
	public static final String CORETHREAD_STREAM = "CoreThread";
	public static final String CONCEPT_STREAM_TAG="Concept";
	public static final String CDATECONCEPT = "CDateConcept";
	public static final String EXECUTE_STREAM_TAG = "CMAExecuteAgent";
	public static final String FLIGHT_DATABASE = "CFlightDatabase";
	
	public static final int ASK_USER_INPUT = 1;
	public static final int CALL_UNDERSTAND = 2;
	public static final int OUTPUT_COMPLETED = 3;
	
	// D: structure describing system actions taken on a particular concept
	public static final String SA_REQUEST = "REQ";
	public static final String SA_EXPL_CONF	="EC";
	public static final String SA_IMPL_CONF	="IC";
	public static final String SA_UNPLANNED_IMPL_CONF= "UIC";
	public static final String SA_OTHER="OTH";
	
	// D: definition of default cardinality
	public static final int DEFAULT_HYPSET_CARDINALITY = 1000;
	// L: definition of Message.what
	public static final int CHANGEBACKGROUND = 1;
	public static final int FINISHDIALOG = 2;
	
	// D: definition of the probability mass that always remains free (is assigned
	//	    to others)
	public static final float FREE_PROB_MASS =((float)0.05);
	// D: define concept update types
	public static final String CU_ASSIGN_FROM_STRING="assign_from_string";
	public static final String CU_ASSIGN_FROM_CONCEPT="assign_from_concept";
	public static final String CU_UPDATE_WITH_CONCEPT="update_with_concept";
	public static final String CU_COLLAPSE_TO_MODE   ="collapse_to_mode";
	public static final String CU_PARTIAL_FROM_STRING="partial_from_string";
	
	public static final String ABSTRACT_CONCEPT ="<ABSTRACT>\n";
	public static final String UNDEFINED_CONCEPT ="<UNDEFINED>\n";
	public static final String INVALIDATED_CONCEPT= "<INVALIDATED>\n";
	public static final String UNDEFINED_VALUE= "<UNDEF_VAL>";
	
	public static final String[] vsGRS = new String[]{
		"UNPROCESSED", "PENDING", "READY", "SCHEDULED", 
        "ONSTACK", "EXECUTING", "DONE"};
	public static final String CityAirport="上海虹桥机场,上海浦东机场,南京禄口机场,南通兴东机场,无锡硕放机场,常州奔牛机场,徐州观音机场,盐城南洋机场,连云港白塔埠机场,杭州萧山机场,宁波栎社机场,温州永强机场,舟山普陀山机场,黄岩路桥机场,衢州机场,义乌机场 ,济南遥墙机场,青岛流亭机场,威海大水泊机场,烟台莱山机场,临沂沐埠岭机场,潍坊南苑机场,东营永安机场,济宁机场,福州长乐机场,厦门高崎机场,泉州晋江机场,龙岩冠豸山机场,武夷山机场 ,南昌昌北机场,赣州黄金机场,九江庐山机场,景德镇罗家机场,井冈山机场,合肥骆岗机场,黄山屯溪机场,安庆天柱山机场,阜阳西关机场 ,北京首都机场,北京南苑机场  ,天津滨海机场,石家庄正定机场,秦皇岛山海关机场 ,太原武宿机场,大同怀仁机场,长治王村机场,运城关公机场,呼和浩特白塔机场,海拉尔东山机场,赤峰土城子,满洲里西郊,乌兰浩特机场,锡林浩特机场,乌海机场,包头二里半,通辽机场,沈阳桃仙机场,大连周水子,锦州小岭子,丹东浪头,朝阳机场 ,长春龙嘉机场,吉林二台子,延吉朝阳川 ,哈尔滨太平机场,齐齐哈尔三家子,佳木斯东郊,牡丹江海浪,黑河机场,西安咸阳机场,汉中西关,延安二十里铺,榆林西沙,安康五里铺  ,兰州中川机场,敦煌机场,嘉峪关机场,庆阳机场 ,西宁曹家堡机场,格尔木机场,银川河东机场,乌鲁木齐地窝铺机场,阿克苏温宿,喀什机场,伊宁机场,塔城机场,阿尔泰机场,库车机场,且末机场,和田机场,库尔勒机场,那拉提机场,富蕴机场,吐鲁番机场,广州白云机场,深圳宝安机场,珠海三灶,汕头外砂,湛江机场,梅县机场,南宁吴圩机场,桂林两江,柳州白莲,北海福城,梧州长洲岛,海口美兰机场,三亚凤凰机场,武汉天河机场,宜昌三峡,荆州沙市,襄樊刘集,恩施许家坪 ,长沙黄花机场,张家界荷花,常德桃花源,永州零陵,怀化芷江,郑州新郑机场,洛阳北郊,南阳姜营,重庆江北机场,万州五桥 ,成都双流机场,泸州蓝田,九寨沟黄龙,攀枝花保安营,南充高坪,宜宾莱坝,绵阳南郊,西昌青山,广元盘龙,达州河市,昆明巫家坝机场,丽江三义机场,德宏芒市,保山云端,迪庆香格里拉机场,西双版纳机场,文山普者黑,大理机场,思茅机场,临沧机场,昭通机场 ,贵阳龙洞堡机场,铜仁大兴,安顺黄果树,兴义机场,黎平机场 ,拉萨贡嘎机场,昌都邦达机场";
	
	
		
	
}
