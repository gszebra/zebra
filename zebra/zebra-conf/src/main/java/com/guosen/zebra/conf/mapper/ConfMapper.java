/**   
* @Title: UserMapper.java 
* @Package com.yy.bg.mapper 
* @Description: TODO(用一句话描述该文件做什么) 
* @author 邓启翔   
* @date 2017年5月18日 下午3:52:47 
* @version V1.0   
*/
package com.guosen.zebra.conf.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.guosen.zebra.conf.dto.ConfCenter;
import com.guosen.zebra.conf.dto.ConfHisCenter;
import com.guosen.zebra.conf.dto.GatwayConf;
import com.guosen.zebra.conf.dto.SentinelDTO;
import com.guosen.zebra.conf.dto.ServerTest;

public interface ConfMapper {
	@Select("SELECT * FROM SERVER_VERSION_CONF")
	public List<Map<String, String>> getServerVersionConf(Map<String, String> param);

	@Update("update SERVER_VERSION_CONF SET SERVER_NAME=#{SERVER_NAME},SERVER_VERSION=#{SERVER_VERSION},SERVER_VERSION_DESC=#{SERVER_VERSION_DESC},UPDDATE=CURRENT_TIMESTAMP() where SID=#{SID}")
	public int updServerVersion(Map<String, Object> param);

	@Insert("INSERT INTO [dbo].[SERVER_VERSION_CONF]([SERVER_NAME],[SERVER_VERSION],[SERVER_VERSION_DESC])VALUES(#{SERVER_NAME},#{SERVER_VERSION},#{SERVER_VERSION_DESC})")
	public int instServerVersion(Map<String, Object> param);

	@Select("select * from SERVER_TEST_CONF where SERVER_NAME=#{server} and METHOD_NAME=#{method}")
	public Map<String, Object> getServerTest(ServerTest param);

	@Update("update SERVER_TEST_CONF set REQUEST =#{request},RESPONSE=#{response},ATTACHMENTS =#{attachments},DESCRIPT=#{descript},UPDDATE=CURRENT_TIMESTAMP() where SERVER_NAME=#{server} and METHOD_NAME=#{method}")
	public int updServerTest(ServerTest param);

	@Insert("INSERT INTO SERVER_TEST_CONF(SERVER_NAME,METHOD_NAME,REQUEST,RESPONSE,ATTACHMENTS,DESCRIPT) values(#{server},#{method},#{request},#{response},#{attachments},#{descript})")
	public int insertServerTest(ServerTest param);

	@Insert("INSERT INTO dbo.GATEWAY_CONF( SERVER_NAME ,SERVER_GROUP ,SERVER_VERSION ,SERVER_SET ,SERVER_PATH,SERVER_TEXT,TAG,GATEWAY_SET)VALUES  ( #{service},#{group},#{version},#{set} ,#{path},#{text},#{tag},#{gatewaySet})")
	public int insertGatewayConf(GatwayConf param);

	@Update("update GATEWAY_CONF set SERVER_NAME =#{service},SERVER_GROUP=#{group},UPDDATE=CURRENT_TIMESTAMP(),SERVER_VERSION=#{version},SERVER_SET=#{set}, SERVER_PATH =#{path},SERVER_TEXT=#{text} ,TAG=#{tag},GATEWAY_SET=#{gatewaySet} where SID=#{sid}")
	public int updGatewayConf(GatwayConf param);

	@Update("delete from GATEWAY_CONF where SID=#{sid}")
	public int delGatewayConf(GatwayConf param);

	@Insert("insert into GATEWAY_CONF_HIS(SID,SERVER_NAME,SERVER_GROUP,SERVER_VERSION,SERVER_SET,SERVER_PATH,SERVER_TEXT,TAG,INSERTDATE,UPDDATE,GATEWAY_SET,VERSION_INFO) select *,#{versionInfo} from GATEWAY_CONF where SID=#{sid}")
	public int bakGatewayConf(GatwayConf param);

	@Select("select SID AS  sid, SERVER_NAME  AS service, SERVER_GROUP AS 'group', SERVER_VERSION AS version,SERVER_SET AS 'set' ,SERVER_PATH path ,SERVER_TEXT text,TAG tag,ISNULL(GATEWAY_SET,'') gatewaySet from GATEWAY_CONF")
	public List<GatwayConf> getGatewayConf();

	@Select("select SID sid,TYPE type, SERVER_NAME server, SERVER_SCOPE scope, SERVER_SCOPE_NAME scopeName,CONF_TEXT text from CONF_CENTER WHERE TYPE=#{type} and SERVER_NAME=#{server} order by SERVER_NAME")
	public List<ConfCenter> getConf(ConfCenter param);

	@Select("select SID sid,TYPE type, SERVER_NAME server, SERVER_SCOPE scope, SERVER_SCOPE_NAME scopeName,CONF_TEXT text from CONF_CENTER WHERE TYPE=#{type} order by SERVER_NAME")
	public List<ConfCenter> getAllConf(ConfCenter param);

	@Select("select SID sid,TYPE type, SERVER_NAME server, SERVER_SCOPE scope, SERVER_SCOPE_NAME scopeName,CONF_TEXT text from CONF_CENTER WHERE SID=#{sid}")
	public ConfCenter getConfById(ConfCenter param);

	@Insert("INSERT INTO CONF_CENTER(TYPE,SERVER_NAME,SERVER_SCOPE,SERVER_SCOPE_NAME,CONF_TEXT)VALUES(#{type},#{server},#{scope},#{scopeName},#{text})")
	@Options(useGeneratedKeys = true, keyProperty = "sid", keyColumn = "SID")
	public int insertConf(ConfCenter param);

	@Update("update CONF_CENTER set SERVER_NAME =#{server},SERVER_SCOPE=#{scope},UPDDATE=CURRENT_TIMESTAMP(),SERVER_SCOPE_NAME=#{scopeName},CONF_TEXT=#{text} where SID=#{sid}")
	public int updConf(ConfCenter param);

	@Delete("delete from CONF_CENTER where SID=#{sid}")
	public int delConf(ConfCenter param);

	@Insert("INSERT INTO CONF_CENTER_HISTORY(SID,TYPE,SERVER_NAME,SERVER_SCOPE,SERVER_SCOPE_NAME,CONF_TEXT,INSERTDATE,UPDDATE,VERSION_INFO) select *,#{versionInfo} from CONF_CENTER where SID=#{sid}")
	public int insertConfHistory(ConfCenter param);

	@Select("select SID sid,TYPE type, SERVER_NAME server, SERVER_SCOPE scope, SERVER_SCOPE_NAME scopeName,CONF_TEXT text,DATE_FORMAT(UPDDATE,'%Y-%m-%d %H:%i:%s') date,VERSION_INFO versionInfo from CONF_CENTER_HISTORY  WHERE SID=#{sid}")
	public List<ConfHisCenter> getHistoryConf(ConfCenter param);

	@Update("update a set a.UPDDATE=CURRENT_TIMESTAMP(),a.SERVER_NAME=b.SERVER_NAME,a.SERVER_SCOPE=b.SERVER_SCOPE,a.SERVER_SCOPE_NAME=b.SERVER_SCOPE_NAME,a.CONF_TEXT=b.CONF_TEXT from CONF_CENTER a,CONF_CENTER_HISTORY b where b.SID=#{sid} and a.SID=b.SID and b.VERSION_INFO=#{versionInfo}")
	public int centerRecovery(ConfCenter param);

	@Update("update SERVER_MANAGER_CONF set UPDDATE=CURRENT_TIMESTAMP(),M_TYPE=#{type},SERVER_NAME=#{serverName},SERVER_IP=#{ip},DATA=#{data} where SID=#{id}")
	public int updSentinel(SentinelDTO param);
	
	@Delete("delete from SERVER_MANAGER_CONF where SID=#{id}")
	public int delSentinel(SentinelDTO param);

	@Insert("INSERT INTO SERVER_MANAGER_CONF( SERVER_NAME ,M_TYPE,SERVER_IP,DATA) VALUES (#{serverName},#{type},#{ip},#{data})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "SID")
	public int createSentinel(SentinelDTO param);

	@Select("select SID id,SERVER_NAME serverName,M_TYPE type,SERVER_IP ip, DATA data from SERVER_MANAGER_CONF")
	public List<SentinelDTO> qrySentinel(SentinelDTO param);

	@Select("select count(*) from CONF_CENTER")
	public int qryConfStatics();

	@Select("select count(*) from SERVER_TEST_CONF")
	public int qryTestConfStatics();

	@Select("select SID AS  sid, SERVER_NAME  AS service, SERVER_GROUP AS 'group', SERVER_VERSION AS version,SERVER_SET AS 'set' ,SERVER_PATH path ,SERVER_TEXT text,TAG tag,IFNULL(GATEWAY_SET,'') gatewaySet,DATE_FORMAT(UPDDATE,'%Y-%m-%d %H:%i:%s') date, VERSION_INFO versionInfo from GATEWAY_CONF_HIS WHERE SID=#{sid}")
	public List<GatwayConf> getGatewayHistConf(ConfCenter param);

	@Update("update a set a.UPDDATE=CURRENT_TIMESTAMP(),a.SERVER_NAME=b.SERVER_NAME,a.SERVER_GROUP=b.SERVER_GROUP ,a.SERVER_VERSION=b.SERVER_VERSION ,a.SERVER_SET=b.SERVER_SET,a.SERVER_PATH=b.SERVER_PATH,a.SERVER_TEXT=b.SERVER_TEXT,a.TAG=b.TAG,a.GATEWAY_SET=b.GATEWAY_SET  from GATEWAY_CONF a,GATEWAY_CONF_HIS b where b.SID=#{sid} and a.SID=b.SID and b.VERSION_INFO=#{versionInfo}")
	public int gatewayConfRecovery(ConfCenter param);
}