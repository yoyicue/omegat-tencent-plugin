package indi.yoyicue.machinetranslators;

import java.awt.Window;
import java.util.TreeMap;

import com.qcloud.QcloudApiModuleCenter;
import com.qcloud.Module.Tmt;
import com.qcloud.Utilities.Json.JSONObject;

import org.omegat.core.Core;
import org.omegat.core.machinetranslators.BaseTranslate;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.Language;

/**
 * Support for Tencent Cloud Translator API machine translation.
 * @author Li Da (yoyciue@gmail.com)
 * @see <a href="https://cloud.tencent.com/document/product/551/7377">Translator API</a>
 */

public class TencentTranslate extends BaseTranslate {

    protected static final String PROPERTY_API_SECRET_ID = "tencent.api.secret.Id";
    protected static final String PROPERTY_API_SECRET_KEY = "tencent.api.secret.Key";

    public static void loadPlugins() {
        Core.registerMachineTranslationClass(TencentTranslate.class);
    }

    public static void unloadPlugins() {
    }

    @Override
    protected String getPreferenceName() {
        return "allow_tencent_translate";
    }

    public String getName() {
        return "Tencent Translate";
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {

        String SecretId = getCredential(PROPERTY_API_SECRET_ID);
        String SecretKey = getCredential(PROPERTY_API_SECRET_KEY);

        TreeMap<String, Object> config = new TreeMap<String, Object>();
        config.put("SecretId", SecretId);
        config.put("SecretKey", SecretKey);
        config.put("RequestMethod", "GET");
        config.put("DefaultRegion", "bj");

        QcloudApiModuleCenter module = new QcloudApiModuleCenter(new Tmt(), config);

        String sourcelang = tmtLang(sLang);
        String targetlang = tmtLang(tLang);

        TreeMap<String, Object> params = new TreeMap<String, Object>();
        params.put("sourceText", text);
        params.put("source", sourcelang);
        params.put("target", targetlang);

        String translation;
        try {
            String result = module.call("TextTranslate", params);
            JSONObject json_result = new JSONObject(result);
            if (json_result.getInt("code") == 0) {
                translation = json_result.getString("targetText");
            } else {
                translation = "Message:"+ json_result.getString("message");
            }
        } catch (NullPointerException e) {
            return null;
        }
        return translation;
    }

    private String tmtLang(Language language) {
        String lang = language.getLanguage();
        if (lang.equalsIgnoreCase("zh-cn")) {
            return "zh";
        } else if (lang.equalsIgnoreCase("ja")) {
            return "jp";
        } else if (lang.equalsIgnoreCase("ko")) {
            return "kr";
        } else {
            return language.getLanguageCode();
        }

    }
    
    @Override
    public boolean isConfigurable() {
        return true;
    }
    
    @Override
    public void showConfigurationUI(Window parent) {
        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                String id = panel.valueField1.getText().trim();
                String key = panel.valueField2.getText().trim();
                boolean temporary = panel.temporaryCheckBox.isSelected();
                setCredential(PROPERTY_API_SECRET_ID, id, temporary);
                setCredential(PROPERTY_API_SECRET_KEY, key, temporary);
            }
        };
        dialog.panel.valueLabel1.setText("secretId");
        dialog.panel.valueField1.setText(getCredential(PROPERTY_API_SECRET_ID));
        dialog.panel.valueLabel2.setText("secretKey");
        dialog.panel.valueField2.setText(getCredential(PROPERTY_API_SECRET_KEY));
        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_API_SECRET_ID));
        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_API_SECRET_KEY));
        dialog.show();
    }
}