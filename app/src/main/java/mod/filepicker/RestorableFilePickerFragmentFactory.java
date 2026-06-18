package mod.filepicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import dev.pranav.filepicker.FilePickerCallback;
import dev.pranav.filepicker.FilePickerDialogFragment;
import dev.pranav.filepicker.FilePickerOptions;

/**
 * Prevents Android from crashing while restoring the third-party file picker.
 *
 * <p>The library fragment only exposes a constructor with options and a callback, while
 * FragmentManager requires an empty constructor during state restoration. Restored picker
 * dialogs are transient and cannot safely recover their callback, so they are instantiated
 * with inert dependencies and removed after the host activity has restored its state.</p>
 */
public final class RestorableFilePickerFragmentFactory extends FragmentFactory {

    private static final String FILE_PICKER_CLASS_NAME =
            FilePickerDialogFragment.class.getName();

    @NonNull
    @Override
    public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
        if (FILE_PICKER_CLASS_NAME.equals(className)) {
            return new FilePickerDialogFragment(
                    new FilePickerOptions(),
                    new FilePickerCallback()
            );
        }
        return super.instantiate(classLoader, className);
    }

    public static void removeRestoredFilePickers(@NonNull FragmentManager fragmentManager) {
        List<Fragment> fragments = new ArrayList<>(fragmentManager.getFragments());
        FragmentTransaction removal = null;

        for (Fragment fragment : fragments) {
            if (fragment instanceof FilePickerDialogFragment) {
                if (removal == null) {
                    removal = fragmentManager.beginTransaction();
                }
                removal.remove(fragment);
            } else if (fragment.isAdded()) {
                removeRestoredFilePickers(fragment.getChildFragmentManager());
            }
        }

        if (removal != null) {
            removal.commitNowAllowingStateLoss();
        }
    }
}
