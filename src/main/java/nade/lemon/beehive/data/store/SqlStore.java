package nade.lemon.beehive.data.store;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import nade.lemon.beehive.LemonBeehive;
import nade.lemon.beehive.api.events.manage.BeehiveCreationEvent;
import nade.lemon.beehive.configuration.BeehiveYamlConfig;
import nade.lemon.beehive.configuration.build.BeehiveConfigBuild;
import nade.lemon.beehive.data.Database;
import nade.lemon.beehive.data.LimitHive;
import nade.lemon.beehive.handlers.HologramHandler;
import nade.lemon.tag.CompoundTag;
import nade.lemon.beehive.objects.BeehiveObject;
import nade.lemon.beehive.objects.CreateReason;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import com.google.common.collect.Sets;

import nade.lemon.utils.Logger;
import nade.lemon.utils.bukkit.Locations;
import nade.lemon.beehive.utils.developer.DeveloperUtils;
import nade.lemon.utils.spigot.Version;

public class SqlStore implements ServiceDataStore{

	static final Set<BeehiveObject> managers = new HashSet<>();
	
	private Connection connection;
	private Class<?> MysqlDataSource = Version.isVersions(18, 19) ? DeveloperUtils.getClass("com.mysql.cj.jdbc.MysqlDataSource") : DeveloperUtils.getClass("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
	private Object source;
	private String host, username, password, database;
	private int port;
	private Exception failException;
	
	private Constructor<?> MysqlDataSourceConstructor = DeveloperUtils.getConstructor(MysqlDataSource);
	
	private Method 
		setServerName = DeveloperUtils.getMethod(MysqlDataSource, "setServerName", String.class),
		setPort = DeveloperUtils.getMethod(MysqlDataSource, "setPort", int.class),
		setUser = DeveloperUtils.getMethod(MysqlDataSource, "setUser", String.class),
		setPassword = DeveloperUtils.getMethod(MysqlDataSource, "setPassword", String.class),
		setDatabaseName = DeveloperUtils.getMethod(MysqlDataSource, "setDatabaseName", String.class),
		getConnection = DeveloperUtils.getMethod(MysqlDataSource, "getConnection");
	
	private enum CALL {
		REMOVE,
		ADD;
	}

	private Logger logger = LemonBeehive.getInstance().get(Logger.class);
	
	public SqlStore() {
		source = DeveloperUtils.newInstance(MysqlDataSourceConstructor);
		String key = "general.mysql";
		BeehiveConfigBuild config = BeehiveYamlConfig.getConfig();
		
		host = config.getOrDefaultConfig(key + ".host", String.class);
		port = config.getOrDefaultConfig(key + ".port", Integer.class);
		database = config.getOrDefaultConfig(key + ".database", String.class);
		username = config.getOrDefaultConfig(key + ".username", String.class);
		password = config.getOrDefaultConfig(key + ".password", String.class);
	}
	
	public boolean connect() {
		try {
			connectMySQL();
			createDatabase();
			connectDatabase();
			createTable();
		} catch (Exception e) {
			failException = e;
			return false;
		}
		return true;
	}
	
	private void connectMySQL() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		DeveloperUtils.run(setServerName, source, host);
		DeveloperUtils.run(setPort, source, port);
		DeveloperUtils.run(setUser, source, username);
		DeveloperUtils.run(setPassword, source, password);
		connection = (Connection) DeveloperUtils.run(getConnection, source);
	}
	
	private Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			DeveloperUtils.run(setServerName, source, host);
			DeveloperUtils.run(setPort, source, port);
			DeveloperUtils.run(setUser, source, username);
			DeveloperUtils.run(setPassword, source, password);
			DeveloperUtils.run(setDatabaseName, source, database);
			return (Connection) DeveloperUtils.run(getConnection, source);
		} catch (ClassNotFoundException ignore) {}
		return null;
	}
	
	private void createDatabase() throws SQLException {
		PreparedStatement statement = connection.prepareStatement("CREATE DATABASE IF NOT EXISTS `%database%`".replace("%database%", database));
		statement.execute();
	}
	
	private void connectDatabase() throws SQLException {
		DeveloperUtils.run(setDatabaseName, source, database);
		connection = (Connection) DeveloperUtils.run(getConnection, source);
	}
	
	private void createTable() throws SQLException {
		PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS `hive` (location VARCHAR(1000), internal LONGTEXT NOT NULL DEFAULT 'none')");
		statement.execute();
	}
	
	public boolean createColumn(String table, String columns, String type) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getColumns(null, null, table, columns);
		if (!result.next()) {
			PreparedStatement statement = connection.prepareStatement("ALTER TABLE `%table%` ADD `%columns%` %type% NOT NULL DEFAULT 'none'".replace("%table%", table).replace("%columns%", columns).replace("%type%", type));
			statement.execute();
			return true;
		}
		return false;
	}
	
	public boolean removeColumn(String table, String columns) throws SQLException{
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getColumns(null, null, table, columns);
		if (result.next()) {
			PreparedStatement statement = connection.prepareStatement("ALTER TABLE `%table%` DROP `%columns%`".replace("%table%", table).replace("%columns%", columns));
			statement.execute();
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param table 
	 * @param columns 
	 * @return element list has been removed
	 * @throws SQLException
	 */
	public Set<String> removeColumns(String table, String... columns) throws SQLException {
		Set<String> removes = Sets.newHashSet();
		for (String column : columns) {
			if (removeColumn(table, column)) removes.add(column);
		}
		return removes;
	}
	
	public boolean hasColumn(String table, String columns) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getColumns(null, null, table, columns);
		if (result.next()) {
			return true;
		}
		return false;
	}
	
	public boolean hasColumn(ResultSet result, String columns) {
		try {
			result.findColumn(columns);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	@Override
	public void remove(BeehiveObject manager) {
		String key = Locations.toJson(manager.getBeehive().getLocation());
		child(CALL.REMOVE, key, manager);
	}
	
	private void remove(String location) {
		child(CALL.REMOVE, location, null);
	}

	@Override
	public void add(BeehiveObject manager) {
		String key = Locations.toJson(manager.getBeehive().getLocation());
		child(CALL.ADD, key, manager);
	}
	
	@Override
	public void update(BeehiveObject manager) {
		update(Locations.toJson(manager.getBeehive().getLocation()), "internal", manager.getTags().toBase64());
	}

	@Override
	public Object get(String key, Location location) {
		CompoundTag tags = CompoundTag.fromBase64(this.get(Locations.toJson(location), "internal"));
		return tags.get(key);
	}

	public int loading() {
		int database = 0;
		try {
			if (!hasColumn("hive", "internal")) createColumn("hive", "internal", "longtext");
			PreparedStatement statement = getConnection().prepareStatement("select * from `hive`");
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				if (!loading(result)) continue;
				database+=1;
			}
			removeColumns("hive", "name", "owner", "information", "friends");
			if (BeehiveYamlConfig.getConfig().getOrDefaultConfig("general.mysql.transfer", Boolean.class)) {
				database += loadYaml();
			}
		} catch (Exception e) {
			logger.warning("MySQL loading failed, please try again.");
			e.printStackTrace();
		}
		return database;
	}
	
	private boolean loading(ResultSet result) throws SQLException {
		Location location = Locations.fromJson(result.getString("location"));
		CompoundTag tags = CompoundTag.fromBase64(result.getString("internal"));
		if (Database.containByLocation(location)) return false;
		if (location.getBlock().getType() != Material.BEEHIVE) {
			remove(tags.getString("location"));
			return false;
		}
		BeehiveObject beehive = new BeehiveObject(tags, CreateReason.LOAD);
		BeehiveCreationEvent created = new BeehiveCreationEvent(beehive, this.getType());
		Bukkit.getPluginManager().callEvent(created);
		if (!created.isCancelled()) {
			HologramHandler.add(beehive.getHolograms());
			this.add(beehive);
			LimitHive.add(beehive.getOwner().getUniqueId());

			beehive.getHiveBees().getBeeTags().forEach((beeTags) -> beehive.getHiveBees().runTask(beeTags));
			Database.addBeehive(location, beehive);
			return true;
		}
		return false;
	}
	
	public int onSave() {
		int size = Database.getBeehives().size();
		Database.getBeehives().clear();
		return size;
	}
	
	private int loadYaml() {
		BeehiveConfigBuild config = BeehiveYamlConfig.getServerDatabase();
		
		int database = 0;
		for (String string : config.getKeys(false)) {
			Location location = Locations.fromJson(string);
			CompoundTag tags = CompoundTag.fromBase64(config.get(string + ".internal", String.class));
			if (Database.containByLocation(location)) continue;
			if (location.getBlock().getType() != Material.BEEHIVE) {
				config.set(string, null);
				continue;
			}
			BeehiveObject beehive = new BeehiveObject(tags, CreateReason.LOAD);
			BeehiveCreationEvent created = new BeehiveCreationEvent(beehive, this.getType());
			Bukkit.getPluginManager().callEvent(created);
			if (created.isCancelled()) {
				continue;
			}
			config.set(string, null);
			Database.addBeehive(location, beehive);
			database+=1;
			
		}
		config.save();
		return database;
	}
	
	
	private void child(CALL call, String key, BeehiveObject manager) {
		try {
			switch (call) {
			case REMOVE:
				PreparedStatement v1 = getConnection().prepareStatement("DELETE FROM `hive` WHERE location = ?");
				v1.setString(1, key);
				v1.execute();
				break;
			case ADD:
				PreparedStatement v2 = getConnection().prepareStatement("INSERT INTO `hive` (location, internal) VALUES (?, ?)");
				v2.setString(1, key);
				v2.setString(2, manager.getTags().toBase64());
				v2.execute();
				break;
			}
		} catch (SQLException e) {
			logger.warning("MySQL edit failed, please try again.");
		}
	}
	
	private void update(String location, String key, String value) {
		try {
			PreparedStatement statement = getConnection().prepareStatement("UPDATE `hive` SET `%key%` = '%value%' WHERE location = ?".replace("%key%", key).replace("%value%", value));
			statement.setString(1, location);
			statement.execute();
		} catch (SQLException | NullPointerException e) {
			logger.warning("MySQL update failed, please try again.");
		}
	}
	
	private String get(String location, String key){
		try {
			PreparedStatement statement = getConnection().prepareStatement("SELECT `%key%` FROM `hive` WHERE location = ?".replace("%key%", key));
			statement.setString(1, location);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				return result.getString(key);
			}
		} catch (SQLException | NullPointerException e) {
			logger.warning("MySQL getting failed, please try again.");
		}
		return "none";
	}
	
	public String getMessage() {
		return getFailException() != null ? getFailException().getMessage() : null;
	}
	
	public Exception getFailException() {
		return failException;
	}

	@Override
	public DataStoreType getType() {
		return DataStoreType.SQL;
	}
}
