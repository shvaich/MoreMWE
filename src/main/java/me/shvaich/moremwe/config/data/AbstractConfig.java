package me.shvaich.moremwe.config.data;

import me.shvaich.moremwe.MoreMWE;
import me.shvaich.moremwe.config.gui.screens.MyConfigGuiScreen;
import me.shvaich.moremwe.gui.data.MyRendererPosition;
import me.shvaich.moremwe.utils.ChatUtil;
import me.shvaich.moremwe.utils.MoreMWEException;
import me.shvaich.moremwe.utils.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class AbstractConfig {

    protected static final String GENERAL_CATEGORY = "General";

    private static final boolean DEFAULT_HIDE_PROPERTY_IF_HAS_DISABLED_DEPENDENCY = false;

    // This should not be static and the way it is unhandled is wrong.
    @ConfigProperty(
            type = PropertyType.SWITCH,
            category = GENERAL_CATEGORY,
            subcategory = "User Interface",
            name = "Hide Property when Dependency is Disabled",
            comment = "Hides a property when a property it depends on is disabled"
    )
    private static boolean hidePropertyIfHasDisabledDependency = DEFAULT_HIDE_PROPERTY_IF_HAS_DISABLED_DEPENDENCY;

    private final Configuration config;
    private final LinkedHashMap<String, LinkedHashMap<String, List<String>>> configStructure = new LinkedHashMap<>();
    private final List<ConfigCategoryContainer> configCategories = new ArrayList<>();
    private final List<ConfigFieldContainer> configFields = new ArrayList<>();
    private final boolean addedInternalProperties;

    protected AbstractConfig(final File file, final String configVersion) { this(file, configVersion, true); }

    protected AbstractConfig(final File file, final String configVersion, final boolean addInternalProperties) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(configVersion);
        final boolean isExistingInstallation = file.exists(); // only needed for MoreMWE

        this.addedInternalProperties = addInternalProperties;
        this.config = new Configuration(file);
        config.load();

        // This part is only needed for MoreMWE
        // since I added this feature in MoreMWE v1.1
        String defaultValue = configVersion;
        if (isExistingInstallation && !configHasProperty("Config", "Version")) {
            defaultValue = "1.0";
        }

        final Property modVersion = config.get("Config", "Version", defaultValue, "Don't Touch This!");
        final String savedVersion = modVersion.getString();

        if (!savedVersion.equals(configVersion)) {
            modVersion.set(configVersion);
            onConfigUpdate(config, savedVersion, configVersion);
        }

        final Map<String, Set<String>> configFieldsNamesMap = new HashMap<>();
        final ConfigFieldContainer hidePropertyIfHasDisabledDependencyField;
        try {
            for (final Field field : this.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(ConfigProperty.class)) {
                    field.setAccessible(true);
                    configFields.add(new ConfigFieldContainer(field, config, configFieldsNamesMap));
                }
                else if (field.isAnnotationPresent(ConfigCategory.class)) {
                    field.setAccessible(true);
                    configCategories.add(new ConfigCategoryContainer(field));
                }
            }

            // very bad (static in instance context)
            if (addInternalProperties) {
                Field field = AbstractConfig.class.getDeclaredField("hidePropertyIfHasDisabledDependency");
                field.setAccessible(true);
                hidePropertyIfHasDisabledDependencyField = new ConfigFieldContainer(field, config, configFieldsNamesMap);
                configFields.add(hidePropertyIfHasDisabledDependencyField);
            }
            else { hidePropertyIfHasDisabledDependencyField = null; }
        }
        catch (Exception e) {
            throw new MoreMWEException(e).sendDebug("Failed to create config");
        }
        try {
            readConfigFieldsInDeclarationOrder(configFieldsNamesMap);
        }
        catch (Exception e) {
            MoreMWE.LOGGER.error("Failed to sort in declaration order", e);
            configStructure.clear();
            readConfigFieldsInAlphabeticalOrder();
        }

        if (hidePropertyIfHasDisabledDependencyField != null) {
            addFieldToStructure(
                    hidePropertyIfHasDisabledDependencyField.getCategory(),
                    hidePropertyIfHasDisabledDependencyField.getSubcategory(),
                    hidePropertyIfHasDisabledDependencyField.getName()
            );
        }

        setConfigPropertiesOrder();

        if (config.hasChanged()) config.save();

        if (hidePropertyIfHasDisabledDependencyField != null) {
            addListener(hidePropertyIfHasDisabledDependencyField, (Object previousValue) -> {
                final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                if (screen instanceof MyConfigGuiScreen)
                    ((MyConfigGuiScreen) screen).onDependencyDisplayTypeChange(hidePropertyIfHasDisabledDependency);
            });

            hidePropertyIf(hidePropertyIfHasDisabledDependencyField, () -> {
                for (final ConfigFieldContainer field : configFields) {
                    if (field != hidePropertyIfHasDisabledDependencyField && !field.isHidden())
                        return false;
                }
                return true;
            });
        }
    }

    public void save() {
        try {
            for (final ConfigFieldContainer field : configFields)
                field.saveFieldValueToConfig();
        }
        catch (Exception e) {
            ChatUtil.addErrorMessage("Failed to save some config!");
            MoreMWE.LOGGER.error("Failed in updating config fields", e);
        }
        if (config.hasChanged()) config.save();
    }

    public void saveOnlyOneProperty(String name) {
        final ConfigFieldContainer field = getConfigFieldOrThrow(name);
        try {
            field.saveFieldValueToConfig();
        }
        catch (Exception e) {
            ChatUtil.addErrorMessage("Failed to save config value!");
            MoreMWE.LOGGER.error("Failed in updating config field", e);
        }
        if (config.hasChanged()) config.save();
    }

    public GuiScreen getConfigGuiScreen() {
        try {
            return new MyConfigGuiScreen(this,
                    configCategories,
                    configFields,
                    configStructure,
                    addedInternalProperties ? hidePropertyIfHasDisabledDependency : DEFAULT_HIDE_PROPERTY_IF_HAS_DISABLED_DEPENDENCY
            );
        }
        catch (Exception e) {
            throw new MoreMWEException(e).sendDebug("Failed to create config GUI");
        }
    }

    private void readConfigFieldsInDeclarationOrder(Map<String, Set<String>> configFieldsNamesMap) throws IOException {
        final Class<?> clazz = this.getClass();
        final InputStream is = clazz.getResourceAsStream('/' + clazz.getName().replace('.', '/') + ".class");
        if (is == null) {
            throw new MoreMWEException();
        }
        final ClassReader cr = new ClassReader(is);
        final ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_CODE);
        final String annotationDesc = Type.getDescriptor(ConfigProperty.class);
        final String rendererPositionDesc = Type.getDescriptor(MyRendererPosition.class);
        for (final FieldNode fieldNode : cn.fields) {
            if (fieldNode.visibleAnnotations == null) continue;
            for (final AnnotationNode annotationNode : fieldNode.visibleAnnotations) {
                if (!annotationNode.desc.equals(annotationDesc)) continue;
                String configCategory = null;
                String configSubcategory = "";
                String configName = null;
                for (int i = 0; i < annotationNode.values.size(); i += 2) {
                    final String name = (String) annotationNode.values.get(i);
                    final Object value = annotationNode.values.get(i+1);
                    if ("category".equals(name) && value instanceof String)
                        configCategory = (String) value;
                    else if ("subcategory".equals(name) && value instanceof  String)
                        configSubcategory = (String) value;
                    else if ("name".equals(name) && value instanceof String)
                        configName = (String) value;
                }
                if (configCategory != null && configName != null) {
                    // (!configFieldsNamesMap.containsKey(configCategory) || !configFieldsNamesMap.get(configCategory).contains(configName))
                    if (!configFieldsNamesMap.computeIfAbsent(configCategory, t -> new HashSet<>()).contains(configName)) {
                        throw new MoreMWEException();
                    }

                    final List<String> subcategoryFields = configStructure.computeIfAbsent(configCategory, t -> new LinkedHashMap<>())
                        .computeIfAbsent(configSubcategory, t -> new ArrayList<>());

                    if (fieldNode.desc.equals(rendererPositionDesc)) {
                        subcategoryFields.add("Show " + configName);
                        subcategoryFields.add(configName + " Xpos");
                        subcategoryFields.add(configName + " Ypos");
                    }
                    subcategoryFields.add(configName);
                }
            }
        }
    }

    private void addFieldToStructure(String category, String subcategory, String name) {
        configStructure.computeIfAbsent(category, t -> new LinkedHashMap<>())
            .computeIfAbsent(subcategory, t -> new ArrayList<>())
                .add(name);
    }

    private void readConfigFieldsInAlphabeticalOrder() {
        final Map<String, Map<String, List<String>>> unsortedStructure = new HashMap<>();
        for (final ConfigFieldContainer fieldData : configFields) {
            final Map<String, List<String>> subcategoriesMap = unsortedStructure.computeIfAbsent(fieldData.getCategory(), t -> new HashMap<>());
            final List<String> subcategoryFields = subcategoriesMap.computeIfAbsent(fieldData.getSubcategory(), t -> new ArrayList<>());
            final String name = fieldData.getName();
            if (fieldData.getType() == PropertyType.HUD_POSITION) {
                subcategoryFields.add("Show " + name);
                subcategoryFields.add(name + " Xpos");
                subcategoryFields.add(name + " Ypos");
            }
            subcategoryFields.add(name);
        }

        for (final String category : getSortedMapKeys(unsortedStructure)) {
            final Map<String, List<String>> subcategoriesMap = unsortedStructure.get(category);
            final LinkedHashMap<String, List<String>> orderedSubcategoriesMap = new LinkedHashMap<>();

            for (final String subcategory : getSortedMapKeys(subcategoriesMap)) {
                final List<String> subcategoryFields = subcategoriesMap.get(subcategory);
                Collections.sort(subcategoryFields);
                orderedSubcategoriesMap.put(subcategory, subcategoryFields);
            }
            configStructure.put(category, orderedSubcategoriesMap);
        }
    }

    private static List<String> getSortedMapKeys(Map<String, ?> map) {
        final List<String> list = new ArrayList<>(map.keySet());
        Collections.sort(list);
        return list;
    }

    private void setConfigPropertiesOrder() {
        for (final Map.Entry<String, LinkedHashMap<String, List<String>>> entry : configStructure.entrySet()) {
            final List<String> propertiesNames = new ArrayList<>();
            for (final List<String> subcategoryFields : entry.getValue().values())
                propertiesNames.addAll(subcategoryFields);

            config.setCategoryPropertyOrder(entry.getKey(), propertiesNames);
        }
    }


    /** Override this to handle Config Updates */
    protected void onConfigUpdate(Configuration config, String savedVersion, String version) {}

    protected void addListener(String name, Consumer<Object> fn) {
        addListener(getConfigFieldOrThrow(name), fn);
    }

    private void addListener(ConfigFieldContainer field, Consumer<Object> fn) {
        field.setAction(fn);
    }

    protected void hidePropertyIf(String name, BooleanSupplier hideCondition) {
        hidePropertyIf(getConfigFieldOrThrow(name), hideCondition);
    }

    private void hidePropertyIf(ConfigFieldContainer field, BooleanSupplier hideCondition) {
        field.setHideCondition(hideCondition);
    }

    protected void addDependency(String dependantName, String dependencyName) {
        getConfigFieldOrThrow(dependantName).setDependsOn(getConfigFieldOrThrow(dependencyName));
    }

    private ConfigFieldContainer getConfigFieldOrThrow(String name) {
        if (name != null) {
            for (final ConfigFieldContainer field : configFields) {
                if (field.getFieldName().equals(name))
                    return field;
            }
        }
        throw new MoreMWEException().sendDebug("Field [{}] not found!", name);
    }

    protected static boolean isVersionLowerThan(String testedVersion, String referenceVersion) {
        return new ComparableVersion(testedVersion).compareTo(new ComparableVersion(referenceVersion)) < 0;
    }

    protected final boolean configHasProperty(String category, String name) {
        return config.hasKey(StringUtil.toLowerCase(category), name);
    }

    /** @return the previous property associated with the category and name, or null if there was no mapping for the category and name. */
    protected final Property removeConfigProperty(String category, String name) {
        category = StringUtil.toLowerCase(category);
        if (config.hasCategory(category)) {
            return config.getCategory(category).remove(name);
        }
        else {
            MoreMWE.LOGGER.debug("Cannot remove property from config file ; category {} doesn't exist in config", category);
            return null;
        }
    }

    protected final void migratePropertyName(String category, String oldName, String newName) {
        category = StringUtil.toLowerCase(category);
        if (config.hasKey(category, oldName)) {
            final net.minecraftforge.common.config.ConfigCategory configCategory = config.getCategory(category);
            final Property oldProperty = configCategory.remove(oldName);
            if (oldProperty != null && !configCategory.containsKey(newName)) {
                oldProperty.setName(newName);
                configCategory.put(newName, oldProperty);
            }
        }
    }
}
