using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using System.Runtime.InteropServices;
using Basic.Managers;
using PureMVC.Patterns;

public class KingjoyIOSHelper : MonoBehaviour
{
#if Kingjoy
    private static string MSG_LOGIN_SUCCESS = "loginSuccess";
    private static string MSG_LOGIN_FAIL = "loginFail";
    private static KingjoyIOSHelper _instance;
    public static KingjoyIOSHelper instance { get { return _instance; } }
    private string _userInfo = "";
    //public GameObject TestObj;
    private static string appId = "c13c3a747d3ec19eee79a1c49a8404e5";   // TODO: 设置您的APPID
    private static bool testUpdate = false;		 // TODO: 测试更新模式。为true时，则不论版本号，肯定提示更新
    private static int updateAlertType = 0;        // TODO: 当检查更新失败时，控制是否允许跳过强制更新； 
    //  0：不提示检查失败（直接跳过并进入游戏） 
    //  1：不允许跳过（alert只有一个"重新检查"按钮） 
    //  2：允许选择跳过更新（alert有两个按钮，一个“否”，一个“重新检查”）
    private static bool logOut = false;		 		 // TODO: 设置控制台打印日志：true为开启，fasle为关闭
	//private static bool _isInit = false;

    void Awake()
    {
        if (_instance == null)
        {
            _instance = this;
            DontDestroyOnLoad(this);
        }
        else
        {
            Destroy(this);
        }
		//_Init();
    }
	
	
	private void Start()
    {
        //XHPaySetUnityReceiver(gameObject.name);
        ZHPayInit(gameObject.name); //SDK init
    }

    Dictionary<string, string> SplitStringToDic(string result)
    {
        Dictionary<string, string> returnDic = new Dictionary<string, string>();
        if (result != "")
        {
            string[] pairs = result.Split(new string[] { "#&#and#&#" }, System.StringSplitOptions.None);

            for (int i = 0; i < pairs.Length; i++)
            {
                string thisPair = pairs[i];
                string[] onePair = thisPair.Split(new string[] { "#=#equal#=#" }, System.StringSplitOptions.None);
                if (onePair.Length == 2)
                {
                    returnDic.Add(onePair[0], onePair[1]);
                }
            }
        }
        return returnDic;
    }

	public string GetUserInfo()
	{
		//Debug.Log ("-------------------------iOS Jailbreak  Helper userInfo 111111111111111111 ");
		//_userInfo = _GetUserInfo();
		Debug.Log ("-------------------------iOS Jailbreak  Helper userInfo : " + _userInfo);
		return _userInfo;
	}
	
	public void SetUserInfo(string userInfo)
	{
		Debug.Log("---------------------- set userInfo ---------------------------------------");
		_userInfo = userInfo;
	}

    #region ZHPaySDK - API部分 -
    public static void ZHPayInit(string callbackName)
    {
        //TODO: API：SDK初始化
        if (Application.platform != RuntimePlatform.OSXEditor)
        {
			Debug.Log("-------- Kingjoy Start init -------");
			Debug.Log ("callbackname is " + callbackName);
			KingjoyInit("10", "appkey" , "7777",callbackName);
        }
    }

    public static void ZHPayStartLogin()
    {
        //TODO: API：开始登陆
        if (Application.platform != RuntimePlatform.OSXEditor)
        {
			Debug.Log("-------- Kingjoy Start Login -------");
			KingjoyStartLogin();
        }
    }
   
	
    public static bool ZHPayStartOrder(string buyInfo)
    {
        //TODO: API：订单支付（订单号，商品名，游戏名，价格，用户自定义参数） 前四个参数不可为空，价格单位为元，最多两位小数
        if (Application.platform != RuntimePlatform.OSXEditor)
        {
//            Facade.Instance.SendNotification(ShopNotes.SHOW_ALERT_WAIT_PAY_CALL_BACK);
            return KingjoyPay(buyInfo);
        }
        return false;
    }

    #endregion

    #region ZHPaySDK - 回调部分 -

    void ZHPayResultSuccessWithOrder(string result)
    {
        // TODO: 回调：订单支付成功
        Dictionary<string, string> orderInfo = SplitStringToDic(result);
        string orderId = orderInfo["orderId"];
        string productName = orderInfo["productName"];
        string productDescription = orderInfo["gameName"];
        string productPrice = orderInfo["productPrice"];
        string userParam = orderInfo["userParam"];
        /*在此可处理您的订单*/
        Facade.Instance.SendNotification(ShopNotes.HIDE_ALERT_WAIT_PAY_CALL_BACK);

        //TestObj.SendMessage("ShowResult","ZHPay回调：订单支付成功："+result,SendMessageOptions.RequireReceiver);
    }

    #endregion

    #region 用C对SDK-API进行封装，不用调用

    [DllImport("__Internal")]
    private static extern void KingjoyInit(string appId, string appKey, string channelID , string callbackName);

	[DllImport("__Internal")]
    private static extern void KingjoyStartLogin();
	
    [DllImport("__Internal")]
    private static extern bool KingjoyPay(string buyInfo);

    #endregion
#endif

}
