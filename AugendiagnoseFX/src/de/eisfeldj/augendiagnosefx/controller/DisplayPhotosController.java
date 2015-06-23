package de.eisfeldj.augendiagnosefx.controller;

import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_FOLDER_PHOTOS;
import static de.eisfeldj.augendiagnosefx.util.PreferenceUtil.KEY_LAST_NAME;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import de.eisfeldj.augendiagnosefx.fxelements.EyePhotoPairNode;
import de.eisfeldj.augendiagnosefx.util.DialogUtil;
import de.eisfeldj.augendiagnosefx.util.DialogUtil.ProgressDialog;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhoto;
import de.eisfeldj.augendiagnosefx.util.imagefile.EyePhotoPair;
import de.eisfeldj.augendiagnosefx.util.Logger;
import de.eisfeldj.augendiagnosefx.util.PreferenceUtil;
import de.eisfeldj.augendiagnosefx.util.ResourceConstants;

/**
 * BaseController for the "Display Photos" page.
 */
public class DisplayPhotosController extends BaseController implements Initializable {
	/**
	 * The list of folder names which should be shown on top of the list.
	 */
	protected static final String[] FOLDERS_TOP = { "TOPOGRAPH", "TOPOGRAF", "IRIDOLOG" };

	/**
	 * The full "Display Photos" pane.
	 */
	@FXML
	private Pane displayMain;

	/**
	 * The list of names.
	 */
	@FXML
	private ListView<String> listNames;

	/**
	 * The list of names.
	 */
	@FXML
	private ListView<GridPane> listPhotos;

	@Override
	public final void initialize(final URL location, final ResourceBundle resources) {
		List<String> valuesNames = getFolderNames(new File(PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS)));
		listNames.setItems(FXCollections.observableList(valuesNames));

		String lastName = PreferenceUtil.getPreferenceString(KEY_LAST_NAME);
		if (lastName != null && valuesNames.contains(lastName)) {
			showPicturesForName(lastName);
			int selectedIndex = valuesNames.indexOf(lastName);
			listNames.getSelectionModel().select(selectedIndex);
			listNames.scrollTo(selectedIndex);
		}
	}

	@Override
	public final Parent getRoot() {
		return displayMain;
	}

	/**
	 * Handler for Click on name on list. Displays the eye photo pairs for that name.
	 *
	 * @param event
	 *            The action event.
	 * @throws IOException
	 */
	@FXML
	protected final void handleNameClick(final MouseEvent event) throws IOException {
		String name = listNames.getSelectionModel().getSelectedItem();
		if (name != null && !name.equals(PreferenceUtil.getPreferenceString(KEY_LAST_NAME))) {
			showPicturesForName(name);
		}
		PreferenceUtil.setPreference(KEY_LAST_NAME, name);
	}

	/**
	 * Display the pictures for a given name.
	 *
	 * @param name
	 *            The name.
	 */
	private void showPicturesForName(final String name) {
		File nameFolder = new File(PreferenceUtil.getPreferenceString(KEY_FOLDER_PHOTOS), name);

		ProgressDialog dialog =
				DialogUtil.displayProgressDialog(ResourceConstants.MESSAGE_PROGRESS_LOADING_PHOTOS, name);

		EyePhotoPair[] eyePhotos = createEyePhotoList(nameFolder);

		ObservableList<GridPane> valuesPhotos = FXCollections.observableList(new ArrayList<GridPane>());

		for (int i = 0; i < eyePhotos.length; i++) {
			EyePhotoPairNode eyePhotoPairNode = new EyePhotoPairNode(eyePhotos[i]);
			valuesPhotos.add(eyePhotoPairNode);

			// Workaround to ensure that the scrollbar is correctly resized after the images are loaded.
			eyePhotoPairNode.getImagesLoadedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(final ObservableValue<? extends Boolean> observable,
						final Boolean oldValue,
						final Boolean newValue) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							GridPane dummy = new GridPane();
							valuesPhotos.add(dummy);
							listPhotos.layout();
							valuesPhotos.remove(dummy);
						}
					});
				}
			});
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				listPhotos.setItems(valuesPhotos);
				dialog.close();
			}
		});
	}

	// METHODS CLONED FROM ANDROID

	/**
	 * Get the list of subfolders, using getFileNameForSorting() for ordering.
	 *
	 * @param parentFolder
	 *            The parent folder.
	 * @return The list of subfolders.
	 */
	public static final List<String> getFolderNames(final File parentFolder) {
		File[] folders = parentFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				return pathname.isDirectory();
			}
		});

		List<String> folderNames = new ArrayList<String>();
		if (folders == null) {
			return folderNames;
		}

		Arrays.sort(folders, new Comparator<File>() {
			@Override
			public int compare(final File f1, final File f2) {
				return getFilenameForSorting(f1).compareTo(getFilenameForSorting(f2));
			}
		});
		for (File f : folders) {
			folderNames.add(f.getName());
		}
		return folderNames;
	}

	/**
	 * Helper method to return the name of the file for sorting.
	 *
	 * @param f
	 *            The file
	 * @return The name for Sorting
	 */
	private static String getFilenameForSorting(final File f) {
		String name = f.getName().toUpperCase(Locale.getDefault());

		boolean sortByLastName = PreferenceUtil.getPreferenceBoolean(PreferenceUtil.KEY_SORT_BY_LAST_NAME);
		if (sortByLastName) {
			int index = name.lastIndexOf(' ');
			if (index >= 0) {
				String firstName = name.substring(0, index);
				String lastName = name.substring(index + 1);
				name = lastName + " " + firstName;
			}
		}

		if (name.indexOf(' ') < 0) {
			for (int i = 0; i < FOLDERS_TOP.length; i++) {
				if (name.contains(FOLDERS_TOP[i])) {
					return "1" + name;
				}
			}
		}
		return "2" + name;
	}

	/**
	 * Create the list of eye photo pairs for display. Photos are arranged in pairs (right-left) by date.
	 *
	 * @param folder
	 *            the folder where the photos are located.
	 * @return The list of eye photo pairs.
	 */
	private EyePhotoPair[] createEyePhotoList(final File folder) {
		Map<Date, EyePhotoPair> eyePhotoMap = new TreeMap<Date, EyePhotoPair>();

		File[] files = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.toUpperCase().endsWith(".JPG");
			}
		});

		if (files == null) {
			return new EyePhotoPair[0];
		}

		for (File f : files) {
			EyePhoto eyePhoto = new EyePhoto(f);

			if (eyePhoto.isFormatted()) {
				Date date = eyePhoto.getDate();

				if (eyePhotoMap.containsKey(date)) {
					EyePhotoPair eyePhotoPair = eyePhotoMap.get(date);
					eyePhotoPair.setEyePhoto(eyePhoto);
				}
				else {
					EyePhotoPair eyePhotoPair = new EyePhotoPair();
					eyePhotoPair.setEyePhoto(eyePhoto);
					eyePhotoMap.put(date, eyePhotoPair);
				}
			}
			else {
				Logger.error("Eye photo is not formatted correctly: " + f.getAbsolutePath());
			}

		}

		// Remove incomplete pairs - need duplication to avoid ConcurrentModificationException
		Map<Date, EyePhotoPair> eyePhotoMap2 = new TreeMap<Date, EyePhotoPair>(new Comparator<Date>() {
			@Override
			public int compare(final Date lhs, final Date rhs) {
				return rhs.compareTo(lhs);
			}
		});

		for (Date date : eyePhotoMap.keySet()) {
			if (eyePhotoMap.get(date).isComplete()) {
				eyePhotoMap2.put(date, eyePhotoMap.get(date));
			}
		}

		return eyePhotoMap2.values().toArray(new EyePhotoPair[eyePhotoMap2.size()]);
	}

}
