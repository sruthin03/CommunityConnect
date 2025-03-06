package model;

import android.content.Context;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {

    private static boolean isInitialized = false; // Ensure it's only initialized once

    public static void init(Context context) {
        if (isInitialized) {
            return; // Prevent multiple initializations
        }

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "drvnwl8p2");  // Replace with your Cloudinary cloud name
        config.put("api_key", "576884477947423");  // Replace with your API key
        config.put("api_secret", "SbpjXuq1fFchikeJ4y-NSNDYVW8"); // Replace with your API secret

        MediaManager.init(context, config);
        isInitialized = true; // Mark initialized
    }
}
