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
 * @since 2018年2月9日 上午11:26:02
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
		// 连接数据库
		connection = MysqlUtils.getMySQLConnectionFromConfiguration(getConfig(), "MySQL", 60, this);
		if (connection == null || !connection.isConnection()) {
			MsgUtils.send("数据库连接失败, 插件已关闭");
			return;
		}
		else {
			// 创建数据库
			connection.createTable(getTableToday(), "name", "points");
			connection.createTable(getTableTotal(), "name", "points");
		}
		
		// 注册监听器
		Bukkit.getPluginManager().registerEvents(new ListenerPlayerPoints(), this);
		
		// 载入语言文件
		language = new Language(this);
		// 创建 API
		totalPointsAPI = new TotoalPointsAPI();
		// 载入 API
		playerPointsAPI = ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI();
		
		// 重置
		new BukkitRunnable() {
			
			public SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			
			@Override
			public void run() {
				// 检查时间
				if (sdf.format(System.currentTimeMillis()).equals(getConfig().getString("Settings.reset"))) {
					// 重置数据
					totalPointsAPI.clearDataToday();
					// 播放公告
					Bukkit.broadcastMessage(language.get("RESET-BROADCAST"));
				}
 			}
		}.runTaskTimer(this, 0, 20 * 60);
		
		// 交互
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
			
			// 检查
			boolean isReward = false;
			// 获取累计充值
			int total = Main.getTotalPointsAPI().getPlayerTotal(player.getName());
			// 循环充值设定
			for (String prePoints : getConfig().getConfigurationSection("Totals").getKeys(false)) {
				// 如果达到目标且没有领取
				if (total >= NumberUtils.getInteger(prePoints) && !Main.getTotalPointsAPI().isPlayerReward(player.getName(), NumberUtils.getInteger(prePoints))) {
					// 添加标记
					player.setMetadata("totalpoints|work", new FixedMetadataValue(this, true));
					// 添加点券
					playerPointsAPI.give(player.getUniqueId(), getConfig().getInt("Totals." + prePoints));
					// 添加标记
					totalPointsAPI.setPlayerReward(player.getName(), NumberUtils.getInteger(prePoints));
					// 提示信息
					player.getPlayer().sendMessage(language.get("POINTS-REWARD")
							.replace("$points", prePoints)
							.replace("$reward", "" + getConfig().getInt("Totals." + prePoints)));
					// 删除标记
					player.removeMetadata("totalpoints|work", this);
					// 检查
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
	 * 获取数据表名称 - 每日
	 * 
	 * @return
	 */
	public static String getTableToday() {
		return inst.getConfig().getString("MySQL.table") + "today";
	}
	
	/**
	 * 获取数据表名称 - 累计
	 * 
	 * @return
	 */
	public static String getTableTotal() {
		return inst.getConfig().getString("MySQL.table") + "total";
	}
}
