package org.job.cookingplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.job.cookingplugin.command.CookCommand;
import org.job.cookingplugin.listener.GUIClickListener;
import org.job.cookingplugin.listener.GUICloseListener;
import org.job.cookingplugin.recipe.RecipeManager;
import org.job.cookingplugin.util.SpecialCropUtil;

import java.io.File;

public class CookingPlugin extends JavaPlugin {

    private static CookingPlugin instance;
    private RecipeManager recipeManager;
    private SpecialCropUtil cropUtil;

    @Override
    public void onEnable() {
        instance = this;

        // ✅ recipes.yml 파일 없으면 자동 복사
        saveDefaultConfigFile("recipes.yml");

        this.recipeManager = new RecipeManager();
        this.recipeManager.loadRecipes(getDataFolder());

        this.cropUtil = new SpecialCropUtil(this);

        getCommand("cook").setExecutor(new CookCommand());
        getServer().getPluginManager().registerEvents(new GUIClickListener(), this);
        getServer().getPluginManager().registerEvents(new GUICloseListener(), this);

        getLogger().info("🍳 요리 플러그인 활성화 완료!");
    }

    private void saveDefaultConfigFile(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
            getLogger().info("✅ " + fileName + " 파일을 생성했습니다.");
        }
    }

    public static CookingPlugin getInstance() {
        return instance;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public SpecialCropUtil getCropUtil() {
        return cropUtil;
    }
}
