package me.skymc.totalpoints.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.Getter;
import me.skymc.taboolib.playerdata.DataUtils;
import me.skymc.totalpoints.Main;

/**
 * @author sky
 * @since 2018年2月9日 下午2:18:36
 */
public class TotoalPointsAPI {
	
	@Getter
	private LinkedHashMap<String, Integer> dataToday = new LinkedHashMap<>();
	
	@Getter
	private LinkedHashMap<String, Integer> dataTotal = new LinkedHashMap<>();
	
	@Getter
	private FileConfiguration data;
	
	/**
	 * 构造方法
	 */
	public TotoalPointsAPI() {
		// 创建配置
		data = DataUtils.addPluginData("data.yml", Main.getInst());
		// 更新任务
		new BukkitRunnable() {
			
			@Override
			public void run() {
				// 获取今日充值
				LinkedList<HashMap<String, Object>> mapToday = Main.getConnection().getValues(Main.getTableToday(), "points", -1, true, "name", "points");
				// 清理数据
				dataToday.clear();
				// 遍历数据
				for (HashMap<String, Object> map : mapToday) {
					dataToday.put(map.get("name").toString(), Integer.valueOf(map.get("points").toString()));
				}
				// 获取累计充值
				LinkedList<HashMap<String, Object>> mapTotal = Main.getConnection().getValues(Main.getTableTotal(), "points", -1, true, "name", "points");
				// 清理数据
				dataTotal.clear();
				// 遍历数据
				for (HashMap<String, Object> map : mapTotal) {
					dataTotal.put(map.get("name").toString(), Integer.valueOf(map.get("points").toString()));
				}
			}
		}.runTaskTimerAsynchronously(Main.getInst(), 0, Main.getInst().getConfig().getInt("Settings.update"));
	}

	/**
	 * 设置玩家累计充值
	 * 
	 * @param player 玩家
	 * @param points 点券
	 * @param append 是否追加数据
	 */
	public void setPlayerTotal(String player, int points, boolean append) {
		// 更新本地数据
		if (append && dataTotal.containsKey(player)) {
			dataTotal.put(player, dataTotal.get(player) + points);
		}
		else {
			dataTotal.put(player, points);
		}
		// 更新SQL数据
		new BukkitRunnable() {
			
			@Override
			public void run() {
				// 如果数据存在
				if (Main.getConnection().isExists(Main.getTableTotal(), "name", player)) {
					Main.getConnection().setValue(Main.getTableTotal(), "name", player, "points", points, append);
				}
				else {
					Main.getConnection().intoValue(Main.getTableTotal(), player, points);
				}
			}
		}.runTaskAsynchronously(Main.getInst());
	}
	
	/**
	 * 设置玩家今日充值
	 * 
	 * @param player 玩家
	 * @param points 点券
	 * @param append 是否追加
	 */
	public void setPlayerToday(String player, int points, boolean append) {
		// 更新本地数据
		if (append && dataToday.containsKey(player)) {
			dataToday.put(player, dataToday.get(player) + points);
		}
		else {
			dataToday.put(player, points);
		}
		// 更新SQL数据
		new BukkitRunnable() {
			
			@Override
			public void run() {
				// 如果数据存在
				if (Main.getConnection().isExists(Main.getTableToday(), "name", player)) {
					Main.getConnection().setValue(Main.getTableToday(), "name", player, "points", points, append);
				}
				else {
					Main.getConnection().intoValue(Main.getTableToday(), player, points);
				}
			}
		}.runTaskAsynchronously(Main.getInst());
	}
	
	/**
	 * 获取玩家累计充值
	 * 
	 * @param player 玩家
	 * @return int
	 */
	public int getPlayerTotal(String player) {
		return dataTotal.containsKey(player) ? dataTotal.get(player) : 0;
	}
	
	/**
	 * 获取玩家今日充值
	 * 
	 * @param player
	 * @return int
	 */
	public int getPlayerToday(String player) {
		return dataToday.containsKey(player) ? dataToday.get(player) : 0;
	}
	
	/**
	 * 玩家是否领取了累计充值奖励
	 * 
	 * @param player 玩家
	 * @param points 额数
	 * @return boolean
	 */
	public boolean isPlayerReward(String player, int points) {
		return data.contains(player) && data.getIntegerList(player).contains(points);
	}
	
	/**
	 * 设置玩家领取了累计奖励
	 * 
	 * @param player
	 * @param points
	 */
	public void setPlayerReward(String player, int points) {
		List<Integer> list = data.contains(player) ? data.getIntegerList(player) : new ArrayList<>();
		if (!list.contains(points)) {
			list.add(points);
			data.set(player, list);
		}
	}
	
	/**
	 * 清理今日充值数据
	 */
	public void clearDataToday() {
		// 清理本地
		dataToday.clear();
		// 清理SQL
		new BukkitRunnable() {
			
			@Override
			public void run() {
				Main.getConnection().truncateTable(Main.getTableToday());
			}
		}.runTaskAsynchronously(Main.getInst());
	}
}
