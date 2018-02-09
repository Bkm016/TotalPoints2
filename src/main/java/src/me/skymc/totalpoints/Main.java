package me.skymc.totalpoints;

import java.text.SimpleDateFormat;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import me.skymc.taboolib.message.MsgUtils;
import me.skymc.taboolib.mysql.MysqlUtils;
import me.skymc.taboolib.mysql.protect.MySQLConnection;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.string.Language;
import me.skymc.totalpoints.api.TotoalPointsAPI;
import me.skymc.totalpoints.listener.ListenerPlayerPoints;
import me.skymc.totalpoints.support.PlaceholderTotal;

/**
 * @author sky
 * @since 2018��2��9�� ����11:26:02
 */
public class Main extends JavaPlugin {
	
	@Getter
	private static Plugin inst;
	
	@Getter
	private static MySQLConnection connection;
	
	@Getter
	private static TotoalPointsAPI totalPointsAPI;
	
	@Getter
	private static PlayerPointsAPI playerPointsAPI;
	
	@Getter
	private static Language language;

	@Override
	public void onLoad() {
		inst = this;
		saveDefaultConfig();
	}
	
	@Override
	public void onEnable() {
		// �������ݿ�
		connection = MysqlUtils.getMySQLConnectionFromConfiguration(getConfig(), "MySQL", 60, this);
		if (connection == null || !connection.isConnection()) {
			MsgUtils.send("���ݿ�����ʧ��, ����ѹر�");
			return;
		}
		else {
			// �������ݿ�
			connection.createTable(getTableToday(), "name", "points");
			connection.createTable(getTableTotal(), "name", "points");
		}
		
		// ע�������
		Bukkit.getPluginManager().registerEvents(new ListenerPlayerPoints(), this);
		
		// ���������ļ�
		language = new Language(this);
		// ���� API
		totalPointsAPI = new TotoalPointsAPI();
		// ���� API
		playerPointsAPI = ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI();
		
		// ����
		new BukkitRunnable() {
			
			public SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			
			@Override
			public void run() {
				// ���ʱ��
				if (sdf.format(System.currentTimeMillis()).equals(getConfig().getString("Settings.reset"))) {
					// ��������
					totalPointsAPI.clearDataToday();
					// ���Ź���
					Bukkit.broadcastMessage(language.get("RESET-BROADCAST"));
				}
 			}
		}.runTaskTimer(this, 0, 20 * 60);
		
		// ����
		new BukkitRunnable() {
			
			@Override
			public void run() {
				new PlaceholderTotal(inst, "totalpoints").hook();
			}
		}.runTask(this);
	}
	
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			// ���
			boolean isReward = false;
			// ��ȡ�ۼƳ�ֵ
			int total = Main.getTotalPointsAPI().getPlayerTotal(player.getName());
			// ѭ����ֵ�趨
			for (String prePoints : getConfig().getConfigurationSection("Totals").getKeys(false)) {
				// ����ﵽĿ����û����ȡ
				if (total >= NumberUtils.getInteger(prePoints) && !Main.getTotalPointsAPI().isPlayerReward(player.getName(), NumberUtils.getInteger(prePoints))) {
					// ��ӱ��
					player.setMetadata("totalpoints|work", new FixedMetadataValue(this, true));
					// ��ӵ�ȯ
					playerPointsAPI.give(player.getUniqueId(), getConfig().getInt("Totals." + prePoints));
					// ��ӱ��
					totalPointsAPI.setPlayerReward(player.getName(), NumberUtils.getInteger(prePoints));
					// ��ʾ��Ϣ
					player.getPlayer().sendMessage(language.get("POINTS-REWARD")
							.replace("$points", prePoints)
							.replace("$reward", "" + getConfig().getInt("Totals." + prePoints)));
					// ɾ�����
					player.removeMetadata("totalpoints|work", this);
					// ���
					isReward = true;
				}
			}
			if (!isReward) {
				language.send(player, "POINTS-EMPTY");
			}
		}
		else {
			reloadConfig();
			sender.sendMessage("reload ok!");
		}
		return true;
	}
	
	/**
	 * ��ȡ���ݱ����� - ÿ��
	 * 
	 * @return
	 */
	public static String getTableToday() {
		return inst.getConfig().getString("MySQL.table") + "today";
	}
	
	/**
	 * ��ȡ���ݱ����� - �ۼ�
	 * 
	 * @return
	 */
	public static String getTableTotal() {
		return inst.getConfig().getString("MySQL.table") + "total";
	}
}
