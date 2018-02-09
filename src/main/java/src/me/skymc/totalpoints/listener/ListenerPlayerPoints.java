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
 * @since 2018��2��9�� ����3:09:18
 */
public class ListenerPlayerPoints implements Listener {
	
	@EventHandler
	public void points(PlayerPointsChangeEvent e) {
		if (e.getChange() > 0) {
			// ��ȡ���
			Player player = Bukkit.getPlayer(e.getPlayerId());
			if (player == null || player.hasMetadata("totalpoints|work")) {
				return;
			}
			
			// ��������
			Main.getTotalPointsAPI().setPlayerTotal(player.getName(), e.getChange(), true);
			Main.getTotalPointsAPI().setPlayerToday(player.getName(), e.getChange(), true);
			
			// ��ȡ�ۼƳ�ֵ
			int total = Main.getTotalPointsAPI().getPlayerTotal(player.getName());
			// ѭ����ֵ�趨
			for (String prePoints : Main.getInst().getConfig().getConfigurationSection("Totals").getKeys(false)) {
				// ����ﵽĿ����û����ȡ
				if (total >= NumberUtils.getInteger(prePoints) && !Main.getTotalPointsAPI().isPlayerReward(player.getName(), NumberUtils.getInteger(prePoints))) {
					// ��ʾ��Ϣ
					player.getPlayer().sendMessage(Main.getLanguage().get("POINTS-NOTIFY").replace("$points", prePoints));
				}
			}
		}
	}
}
