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
 * @since 2018��2��9�� ����2:18:36
 */
public class TotoalPointsAPI {
	
	@Getter
	private LinkedHashMap<String, Integer> dataToday = new LinkedHashMap<>();
	
	@Getter
	private LinkedHashMap<String, Integer> dataTotal = new LinkedHashMap<>();
	
	@Getter
	private FileConfiguration data;
	
	/**
	 * ���췽��
	 */
	public TotoalPointsAPI() {
		// ��������
		data = DataUtils.addPluginData("data.yml", Main.getInst());
		// ��������
		new BukkitRunnable() {
			
			@Override
			public void run() {
				// ��ȡ���ճ�ֵ
				LinkedList<HashMap<String, Object>> mapToday = Main.getConnection().getValues(Main.getTableToday(), "points", -1, true, "name", "points");
				// ��������
				dataToday.clear();
				// ��������
				for (HashMap<String, Object> map : mapToday) {
					dataToday.put(map.get("name").toString(), Integer.valueOf(map.get("points").toString()));
				}
				// ��ȡ�ۼƳ�ֵ
				LinkedList<HashMap<String, Object>> mapTotal = Main.getConnection().getValues(Main.getTableTotal(), "points", -1, true, "name", "points");
				// ��������
				dataTotal.clear();
				// ��������
				for (HashMap<String, Object> map : mapTotal) {
					dataTotal.put(map.get("name").toString(), Integer.valueOf(map.get("points").toString()));
				}
			}
		}.runTaskTimerAsynchronously(Main.getInst(), 0, Main.getInst().getConfig().getInt("Settings.update"));
	}

	/**
	 * ��������ۼƳ�ֵ
	 * 
	 * @param player ���
	 * @param points ��ȯ
	 * @param append �Ƿ�׷������
	 */
	public void setPlayerTotal(String player, int points, boolean append) {
		// ���±�������
		if (append && dataTotal.containsKey(player)) {
			dataTotal.put(player, dataTotal.get(player) + points);
		}
		else {
			dataTotal.put(player, points);
		}
		// ����SQL����
		new BukkitRunnable() {
			
			@Override
			public void run() {
				// ������ݴ���
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
	 * ������ҽ��ճ�ֵ
	 * 
	 * @param player ���
	 * @param points ��ȯ
	 * @param append �Ƿ�׷��
	 */
	public void setPlayerToday(String player, int points, boolean append) {
		// ���±�������
		if (append && dataToday.containsKey(player)) {
			dataToday.put(player, dataToday.get(player) + points);
		}
		else {
			dataToday.put(player, points);
		}
		// ����SQL����
		new BukkitRunnable() {
			
			@Override
			public void run() {
				// ������ݴ���
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
	 * ��ȡ����ۼƳ�ֵ
	 * 
	 * @param player ���
	 * @return int
	 */
	public int getPlayerTotal(String player) {
		return dataTotal.containsKey(player) ? dataTotal.get(player) : 0;
	}
	
	/**
	 * ��ȡ��ҽ��ճ�ֵ
	 * 
	 * @param player
	 * @return int
	 */
	public int getPlayerToday(String player) {
		return dataToday.containsKey(player) ? dataToday.get(player) : 0;
	}
	
	/**
	 * ����Ƿ���ȡ���ۼƳ�ֵ����
	 * 
	 * @param player ���
	 * @param points ����
	 * @return boolean
	 */
	public boolean isPlayerReward(String player, int points) {
		return data.contains(player) && data.getIntegerList(player).contains(points);
	}
	
	/**
	 * ���������ȡ���ۼƽ���
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
	 * ������ճ�ֵ����
	 */
	public void clearDataToday() {
		// ������
		dataToday.clear();
		// ����SQL
		new BukkitRunnable() {
			
			@Override
			public void run() {
				Main.getConnection().truncateTable(Main.getTableToday());
			}
		}.runTaskAsynchronously(Main.getInst());
	}
}
