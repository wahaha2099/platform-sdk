using UnityEngine;
using System.Collections;

public class Kingjoy_Kingjoy_IOSSDKHelper : IOSSDKParent
{

#if UNITY_IPHONE && Kingjoy

	protected override void Awake()
	{
		base.Awake();
		gameObject.AddComponent<KingjoyIOSHelper>();
	}
	
	public override void ShowLoginView ()
    {
        // TODO: haima login
		KingjoyIOSHelper.ZHPayStartLogin();
    }

    protected override string _GetUserInfo ()
    {
		return KingjoyIOSHelper.instance.GetUserInfo();
    }

    public override void Buy(params string[] info)
    {
		LitJson.JsonData data = LitJson.JsonMapper.ToObject(info[2]);
		int productId = (int)data["productId"];
		int price = (int)data["price"];
		ShopCommodity temp = ShopGoodsManager.instance.GetCommodityById((long)productId);
		Log.Debug("---------------------productId " + productId  + " ---- price -------  : " + price);
		
		if (GameConfig.isRelease) 
		{
			price *= 100;
		}
		else
		{
			price = 1;
		}
		string buyInfo = price + "&" + info[1] + "&" + temp.commodity_name + "&" + info[0] + "&" + info[2];
		KingjoyIOSHelper.ZHPayStartOrder(buyInfo); 
    }
#endif

}
