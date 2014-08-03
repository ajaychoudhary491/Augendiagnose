package de.eisfeldj.augendiagnose.activities;

import android.os.Bundle;
import android.view.View;
import de.eisfeldj.augendiagnose.R;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameBaseFragment;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameFragment;
import de.eisfeldj.augendiagnose.fragments.ListPicturesForNameFragment.ListPicturesForNameFragmentHolder;

/**
 * Base activity to display the pictures in an eye photo folder (in pairs) Abstract class - child classes determine the
 * detailed actions.
 */
public abstract class ListPicturesForNameBaseActivity extends BaseActivity implements
		ListPicturesForNameFragmentHolder {
	/**
	 * The resource key for the name of the folder/person to be displayed.
	 */
	protected static final String STRING_EXTRA_NAME = "de.eisfeldj.augendiagnose.NAME";
	/**
	 * The resource key for the parent folder of eye images.
	 */
	protected static final String STRING_EXTRA_PARENTFOLDER = "de.eisfeldj.augendiagnose.PARENTFOLDER";

	/**
	 * The fragment tag.
	 */
	private static final String FRAGMENT_TAG = "FRAGMENT_TAG";

	/**
	 * The fragment displaying the pictures.
	 */
	private ListPicturesForNameBaseFragment fragment;

	// OVERRIDABLE
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String name = getIntent().getStringExtra(STRING_EXTRA_NAME);
		String parentFolder = getIntent().getStringExtra(STRING_EXTRA_PARENTFOLDER);

		setContentView(R.layout.activity_fragments_single);

		fragment = (ListPicturesForNameBaseFragment) getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

		if (fragment == null) {
			fragment = createFragment();
			fragment.setParameters(parentFolder, name);

			getFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, FRAGMENT_TAG).commit();
			getFragmentManager().executePendingTransactions();
		}
	}

	/**
	 * Factory method to retrieve the ListPicturesForNameBaseFragment.
	 *
	 * @return The ListPicturesForNameBaseFragment to be used.
	 */
	protected abstract ListPicturesForNameBaseFragment createFragment();

	/**
	 * onClick action for Button "additional pictures".
	 *
	 * @param view
	 *            The view triggering the onClick action.
	 */
	public final void selectDifferentPictureActivity(final View view) {
		ListFoldersForDisplaySecondActivity.startActivity(this, fragment.getParentFolder());
	}

	@Override
	protected final int getHelpResource() {
		return R.string.html_display_photos;
	}

	// implementation of ListPicturesForNameFragmentHolder

	@Override
	public final ListPicturesForNameFragment getListPicturesForNameFragment() {
		return (ListPicturesForNameFragment) fragment;
	}

	@Override
	public final void setListPicturesForNameFragment(final ListPicturesForNameFragment listPicturesForNameFragment) {
		this.fragment = listPicturesForNameFragment;
	}
}
