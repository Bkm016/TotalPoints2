package me.skymc.totalpoints.listener;

import org.black_ixx.playerpoints.event.PlayerPointsChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.skymc.taboolib.other.NumberUtils;
import me.skymc.totalpoints.Main;

/**
 * @author sky
 * @since 2018年2月9日 下午3:09:18
 */
public class ListenerPlayerPoints implements Listener {
	
	@EventHandler
	public void points(PlayerPointsChangeEvent e) {
		if (e.getChange() > 0) {
			// 获取玩家
			Player player = Bukkit.getPlayer(e.getPlayerId());
			if (player == null || player.hasMetadata("totalpoints|work")) {
				return;
			}
			
			// 更新数据
			Main.getTotalPointsAPI().setPlayerTotal(player.getName(), e.getChange(), true);
			Main.getTotalPointsAPI().setPlayerToday(player.getName(), e.getChange(), true);
			
			// 获取累计充值
			int total = Main.getTotalPointsAPI().getPlayerTotal(player.getName());
			// 循环充值设定
			for (String prePoints : Main.getInst().getConfig().getConfigurationSection("Totals").getKeys(false)) {
				// 如果达到目标且没有领取
				if (total >= NumberUtils.getInteger(prePoints) && !Main.getTotalPointsAPI().isPlayerReward(player.getName(), NumberUtils.getInteger(prePoints))) {
					// 提示信息
					player.getPlayer().sendMessage(Main.getLanguage().get("POINTS-NOTIFY").replace("$points", prePoints));
				}
			}
		}
	}
}
