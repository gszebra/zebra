package com.guosen.zebra.core.registry.etcd.cmd;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.coreos.jetcd.data.ByteSequence;
import com.guosen.zebra.core.common.ZebraConstants;
import com.guosen.zebra.core.registry.etcd.EtcdClient;

public class CommandDel {
	private static final Logger log = LogManager.getLogger(CommandDel.class);
	public void del(Map<String, Object> keyValue) throws Exception {
		log.debug("CommandPut keyValue={}", keyValue);
		EtcdClient.getSingleton().client.getKVClient()
				.delete(ByteSequence.fromString((String)keyValue.get(ZebraConstants.KEY)));
	}
}
