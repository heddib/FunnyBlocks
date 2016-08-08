package fr.heddib.updater;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Joiner;

/**
 * A simple auto-updater.
 * <br><br>Thanks to Gravity for his updater (this file use some parts of his code) !
 * <br><br>And thanks to Hugo =P
 * 
 * @author heddib
 * @author Skyost
 */

public class HeddiBUpdater {
	
	private final Plugin plugin;
	private final String id;
	private final File pluginFile;
	private final boolean download;
	private final boolean announce;
	private final Logger logger;
	private final File updateFolder;
	private URL url;
	
	private final YamlConfiguration config = new YamlConfiguration();
	
	private Result result = Result.SUCCESS;
	private String[] updateData;
	private String response;
	private Thread updaterThread;
	
	private static final String HeddiBUpdater_VERSION = "0.5.1";
	
	public enum Result {
		
		/**
		 * A new version has been found, downloaded and will be loaded at the next server reload / restart.
		 */
		
		SUCCESS,
		
		/**
		 * A new version has been found but nothing was downloaded.
		 */
		
		UPDATE_AVAILABLE,
		
		/**
		 * No update found.
		 */
		
		NO_UPDATE,
		
		/**
		 * The updater is disabled.
		 */
		
		DISABLED,
		
		/**
		 * An error occured.
		 */
		
		ERROR;
	}
	
	public enum InfoType {
		
		/**
		 * Gets the download URL.
		 */
		
		DOWNLOAD_URL,
		
		/**
		 * Gets the file name.
		 */
		
		FILE_NAME;
	}
	
	/**
	 * Initializes HeddiBUpdater.
	 * 
	 * @param plugin Your plugin.
	 * @param id Your plugin ID on HeddiB.
	 * @param pluginFile The plugin file. You can get it from your plugin using <i>getFile()</i>.
	 * @param download If you want to download the file.
	 * @param announce If you want to announce the progress of the update.
	 * @throws IOException InputOutputException.
	 * @throws InvalidConfigurationException If there is a problem with HeddiBUpdater's config.
	 */
	
	public HeddiBUpdater(final Plugin plugin, final String id, final File pluginFile, final boolean download, final boolean announce) throws IOException, InvalidConfigurationException {
		this.plugin = plugin;
		this.id = id;
		this.pluginFile = pluginFile;
		this.download = download;
		this.announce = announce;
		logger = Bukkit.getLogger();
		updateFolder = Bukkit.getUpdateFolderFile();
		if(!updateFolder.exists()) {
			updateFolder.mkdir();
		}
		final File HeddiBUpdaterFolder = new File(plugin.getDataFolder().getParentFile(), "HeddiBUpdater");
		if(!HeddiBUpdaterFolder.exists()) {
			HeddiBUpdaterFolder.mkdir();
		}
		final String lineSeparator = System.lineSeparator();
		final StringBuilder header = new StringBuilder();
		header.append("HeddiBUpdater configuration - By heddib" + lineSeparator + lineSeparator);
		header.append("What is HeddiBUpdater ?" + lineSeparator);
		header.append("HeddiBUpdater is a simple updater created by heddib used to auto-update his Bukkit Plugins." + lineSeparator + lineSeparator);
		header.append("What happens during the update process ?" + lineSeparator);
		header.append("1 - Connection to heddib.tk." + lineSeparator);
		header.append("2 - Plugin version compared against version on heddib.tk." + lineSeparator);
		header.append("3 - Downloading of the plugin from heddib.tk if a newer version is found." + lineSeparator + lineSeparator);
		header.append("So what is this file ?" + lineSeparator);
		header.append("This file is just a config file for this auto-updater." + lineSeparator + lineSeparator);
		header.append("Configuration :" + lineSeparator);
		header.append("'enable': Choose if you want to enable the auto-updater." + lineSeparator);
		header.append("Good game, I hope you will enjoy your plugins always up-to-date ;)" + lineSeparator);
		final File configFile = new File(HeddiBUpdaterFolder, "HeddiBUpdater.yml");
		if(!configFile.exists()) {
			configFile.createNewFile();
			config.options().header(header.toString());
			config.set("enable", true);
			config.save(configFile);
		}
		config.load(configFile);
		if(!config.getBoolean("enable")) {
			result = Result.DISABLED;
			if(announce) {
				logger.log(Level.INFO, "[HeddiBUpdater] HeddiBUpdater is disabled.");
			}
			return;
		}
		url = new URL("http://heddib.tk/plugins/api/?id=" + id);
		updaterThread = new Thread(new UpdaterThread());
		updaterThread.start();
	}
	
	/**
	 * Gets the version of HeddiBUpdater.
	 * 
	 * @return The version of HeddiBUpdater.
	 */
	
	public static final String getVersion() {
		return HeddiBUpdater_VERSION;
	}
	
	/**
	 * Gets the result of HeddiBUpdater.
	 * 
	 * @return The result of the update process.
	 */
	
	public final Result getResult() {
		waitForThread();
		return result;
	}
	
	/**
	 * Gets informations about the latest file.
	 * 
	 * @param type The type of information you want.
	 * 
	 * @return The information you want.
	 */
	
	public final String getLatestFileInfo(final InfoType type) {
		waitForThread();
		switch(type) {
		case DOWNLOAD_URL:
			return updateData[0];
		case FILE_NAME:
			return updateData[1];
		}
		return null;
	}
	
	/**
	 * Gets raw data about the latest file.
	 * 
	 * @return An array string which contains every of the update process.
	 */
	
	public final String[] getLatestFileData() {
		waitForThread();
		return updateData;
	}
	
	/**
	 * Downloads a file.
	 * 
	 * @param site The URL of the file you want to download.
	 * @param pathTo The path where you want the file to be downloaded.
	 * 
	 * @return <b>true</b>If the download was a success.
	 * </b>false</b>If there is an error during the download.
	 * 
	 * @throws IOException InputOutputException.
	 */
	
	private final boolean download(final String site, final File pathTo) {
		try {
			final HttpURLConnection connection = (HttpURLConnection)new URL(site).openConnection();
			connection.addRequestProperty("User-Agent", "HeddiBUpdater v" + HeddiBUpdater_VERSION);
			response = connection.getResponseCode() + " " + connection.getResponseMessage();
			if(!response.startsWith("2")) {
				if(announce) {
					logger.log(Level.INFO, "[HeddiBUpdater] Bad response : '" + response + "' when trying to download the update.");
				}
				result = Result.ERROR;
				return false;
			}
			final long size = connection.getContentLengthLong();
			final long koSize = size / 1000;
			long lastPercent = 0;
			long percent = 0;
			float totalDataRead = 0;
			final InputStream inputStream = connection.getInputStream();
			final FileOutputStream fileOutputStream = new FileOutputStream(pathTo);
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 1024);
			final byte[] data = new byte[1024];
			int i = 0;
			while((i = inputStream.read(data, 0, 1024)) >= 0) {
				totalDataRead += i;
				bufferedOutputStream.write(data, 0, i);
				if(announce) {
					percent = ((long)(totalDataRead * 100) / size);
					if(lastPercent != percent) {
						lastPercent = percent;
						logger.log(Level.INFO, "[HeddiBUpdater] " + percent + "% of " + koSize + "ko...");
					}
				}
			}
			bufferedOutputStream.close();
			fileOutputStream.close();
			inputStream.close();
			return true;
		}
		catch(final Exception ex) {
			logger.log(Level.SEVERE, "Exception '" + ex + "' occured when downloading update. Please check your network connection.");
			result = Result.ERROR;
		}
		return false;
	}
	
	/**
	 * Compares two versions.
	 * 
	 * @param version1 The version you want to compare to.
	 * @param version2 The version you want to compare with.
	 * 
	 * @return <b>true</b> If <b>versionTo</b> is inferior than <b>versionWith</b>.
	 * <br><b>false</b> If <b>versionTo</b> is superior or equals to <b>versionWith</b>.
	 */
	
	public static final boolean compareVersions(final String versionTo, final String versionWith) {
		return normalisedVersion(versionTo, ".", 4).compareTo(normalisedVersion(versionWith, ".", 4)) > 0;
	}
	
	/**
	 * Gets the formatted name of a version.
	 * <br>Used for the method <b>compareVersions(...)</b> of this class.
	 * 
	 * @param version The version you want to format.
	 * @param separator The separator between the numbers of this version.
	 * @param maxWidth The max width of the formatted version.
	 * 
	 * @return A string which the formatted version of your version.
	 * 
	 * @author Peter Lawrey.
	 */

	private static final String normalisedVersion(final String version, final String separator, final int maxWidth) {
		final StringBuilder stringBuilder = new StringBuilder();
		for(final String normalised : Pattern.compile(separator, Pattern.LITERAL).split(version)) {
			stringBuilder.append(String.format("%" + maxWidth + 's', normalised));
		}
		return stringBuilder.toString();
	}
	
	/**
	 * As the result of Updater output depends on the thread's completion,
	 * <br>it is necessary to wait for the thread to finish before allowing anyone to check the result.
	 * 
	 * @author <b>Gravity</b> from his Updater.
	 */
	
	private final void waitForThread() {
		if(updaterThread != null && updaterThread.isAlive()) {
			try {
				updaterThread.join();
			}
			catch(InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	private class UpdaterThread implements Runnable {
	
		@Override
		public void run() {
			try {
				final String pluginName = plugin.getName().replace("_", " ");
				final HttpURLConnection con = (HttpURLConnection)url.openConnection();
				con.addRequestProperty("User-Agent", "HeddiBUpdater v" + HeddiBUpdater_VERSION);
				response = con.getResponseCode() + " " + con.getResponseMessage();
				if(!response.startsWith("2")) {
					if(announce) {
						logger.log(Level.INFO, "[HeddiBUpdater] Bad response : '" + response + ".");
					}
					result = Result.ERROR;
					return;
				}
				final InputStreamReader inputStreamReader = new InputStreamReader(con.getInputStream());
				final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				final String response = bufferedReader.readLine();
				if(response != null && !response.equals("[]")) {
					final JSONObject jsonObject = (JSONObject)JSONValue.parseWithException(response);
					updateData = new String[]{String.valueOf(jsonObject.get("downloadUrl")), String.valueOf(jsonObject.get("fileName")), String.valueOf(jsonObject.get("version"))};
					if(compareVersions(updateData[2], plugin.getDescription().getVersion()) && updateData[0].toLowerCase().endsWith(".jar")) {
						result = Result.UPDATE_AVAILABLE;
						if(download) {
							if(announce) {
								logger.log(Level.INFO, "[HeddiBUpdater] Downloading a new update : " + updateData[2] + "...");
							}
							if(download(updateData[0], new File(updateFolder, pluginFile.getName()))) {
								result = Result.SUCCESS;
								if(announce) {
									logger.log(Level.INFO, "[HeddiBUpdater] The update of '" + pluginName + "' has been downloaded and installed. It will be loaded at the next server load / reload.");
								}
							}
							else {
								result = Result.ERROR;
							}
						}
						else if(announce) {
							logger.log(Level.INFO, "[HeddiBUpdater] An update has been found for '" + pluginName + "' but nothing was downloaded.");
						}
						return;
					}
					else {
						result = Result.NO_UPDATE;
						if(announce) {
							logger.log(Level.INFO, "[HeddiBUpdater] No update found for '" + pluginName + "'.");
						}
					}
				}
				else {
					logger.log(Level.SEVERE, "[HeddiBUpdater] The ID '" + id + "' was not found (or no files found for this project) ! Maybe the author(s) (" + Joiner.on(", ").join(plugin.getDescription().getAuthors()) + ") of '" + pluginName + "' has/have misconfigured his/their plugin ?");
					result = Result.ERROR;
				}
				bufferedReader.close();
				inputStreamReader.close();
			}
			catch(final Exception ex) {
				logger.log(Level.SEVERE, "[HeddiBUpdater] Exception '" + ex + "'. Please check your network connection.");
				ex.printStackTrace();
				result = Result.ERROR;
			}
		}
		
	}

}
