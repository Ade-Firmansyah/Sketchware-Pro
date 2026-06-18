package pro.sketchware.fragments.settings.language;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

import a.a.a.qA;
import mod.localization.LocaleHelper;
import pro.sketchware.R;
import pro.sketchware.databinding.FragmentSettingsLanguageBinding;

public class SettingsLanguageFragment extends qA {

    private static final String[] LANGUAGE_CODES = {
            "en", "in", "hi", "es", "pt", "ar", "ru", "ja", "ko", "zh-rCN", "tr",
            "de", "fr", "it"
    };

    private FragmentSettingsLanguageBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSettingsLanguageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureToolbar(binding.toolbar);
        applyWindowInsets();
        populateLanguages();
    }

    private void applyWindowInsets() {
        View appBar = binding.appBarLayout;
        int appBarLeft = appBar.getPaddingLeft();
        int appBarTop = appBar.getPaddingTop();
        int appBarRight = appBar.getPaddingRight();
        int appBarBottom = appBar.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
            );
            view.setPadding(
                    appBarLeft + insets.left,
                    appBarTop + insets.top,
                    appBarRight + insets.right,
                    appBarBottom
            );
            return windowInsets;
        });

        View content = binding.content;
        int contentLeft = content.getPaddingLeft();
        int contentTop = content.getPaddingTop();
        int contentRight = content.getPaddingRight();
        int contentBottom = content.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    contentLeft + insets.left,
                    contentTop,
                    contentRight + insets.right,
                    contentBottom + insets.bottom
            );
            return windowInsets;
        });
    }

    private void populateLanguages() {
        String[] languageNames = getResources().getStringArray(R.array.language_names);
        String currentLanguage = LocaleHelper.getCurrentLanguage(requireContext());

        for (int index = 0; index < LANGUAGE_CODES.length; index++) {
            String languageCode = LANGUAGE_CODES[index];
            View item = getLayoutInflater().inflate(
                    R.layout.item_settings_language,
                    binding.languageList,
                    false
            );
            MaterialCardView card = item.findViewById(R.id.language_card);
            TextView languageName = item.findViewById(R.id.language_name);
            TextView languageTag = item.findViewById(R.id.language_tag);
            RadioButton selected = item.findViewById(R.id.language_selected);

            languageName.setText(languageNames[index]);
            languageTag.setText(displayLanguageTag(languageCode));
            boolean isSelected = languageCode.equals(currentLanguage);
            card.setChecked(isSelected);
            selected.setChecked(isSelected);

            card.setOnClickListener(clicked -> selectLanguage(languageCode));
            binding.languageList.addView(item);
        }
    }

    private void selectLanguage(@NonNull String languageCode) {
        if (languageCode.equals(LocaleHelper.getCurrentLanguage(requireContext()))) {
            return;
        }
        LocaleHelper.setLocale(requireContext(), languageCode);
        requireActivity().recreate();
    }

    @NonNull
    private String displayLanguageTag(@NonNull String languageCode) {
        return switch (languageCode) {
            case "in" -> "ID";
            case "zh-rCN" -> "ZH-CN";
            default -> languageCode.toUpperCase(java.util.Locale.ROOT);
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
