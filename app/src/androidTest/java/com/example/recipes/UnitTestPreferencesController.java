//package com.example.recipes;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.res.Configuration;
//import android.content.res.Resources;
//
//import com.example.recipes.Controller.PreferencesController;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//public class UnitTestPreferencesController {
//    @Mock Context mockContext;
//    @Mock SharedPreferences mockSharedPreferences;
//    @Mock SharedPreferences.Editor mockEditor;
//    @Mock Resources mockResources;
//
//    private PreferencesController controller;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        when(mockContext.getSharedPreferences("setting", Context.MODE_PRIVATE)).thenReturn(mockSharedPreferences);
//        when(mockContext.getResources()).thenReturn(mockResources);
//        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
//        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
//        when(mockContext.getResources()).thenReturn(mockResources);
//        when(mockResources.getStringArray(R.array.language_values)).thenReturn(new String[]{"uk", "en"});
//        when(mockResources.getStringArray(R.array.theme_options)).thenReturn(new String[]{"Light", "Dark"});
//        when(mockResources.getStringArray(R.array.palette_options)).thenReturn(new String[]{"Brown", "Blue", "Green", "Purple"});
//
//        controller = new PreferencesController();
//    }
//
//    @Test
//    public void testLoadPreferences() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//
//        assertEquals("en", controller.getLanguageString());
//        assertEquals("Dark", controller.getThemeString());
//        assertEquals("Blue", controller.getPaletteString());
//    }
//
//    @Test
//    public void testSavePreferences() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.savePreferences("uk", "Light", "Brown");
//
//        verify(mockEditor).putString(controller.language_key, "uk");
//        verify(mockEditor).putString(controller.theme_key, "Light");
//        verify(mockEditor).putString(controller.palette_key, "Brown");
//        verify(mockEditor).apply();
//    }
//
//    @Test
//    public void testSetLocale() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.setLocale("fr");
//
//        verify(mockResources, times(2)).updateConfiguration(any(Configuration.class), any());
//        verify(mockResources, times(2)).getDisplayMetrics();
//    }
//
//    @Test
//    public void testSetAppThemeBrownLight() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.setAppTheme("Light", "Brown");
//
//        verify(mockContext).setTheme(R.style.AppTheme_Brown_Light);
//    }
//
//    @Test
//    public void testSetAppThemeBrownDark() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.setAppTheme("Dark", "Brown");
//
//        verify(mockContext).setTheme(R.style.AppTheme_Brown_Dark);
//    }
//
//    @Test
//    public void testSetAppThemeBlueLight() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.setAppTheme("Light", "Blue");
//
//        verify(mockContext).setTheme(R.style.AppTheme_Blue_Light);
//    }
//
//    @Test
//    public void testSetAppThemeBlueDark() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.setAppTheme("Dark", "Blue");
//
//        verify(mockContext, times(2)).setTheme(R.style.AppTheme_Blue_Dark);
//    }
//
//    @Test
//    public void testSetAppThemePurpleLight() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.setAppTheme("Light", "Purple");
//
//        verify(mockContext).setTheme(R.style.AppTheme_Purple_Light);
//    }
//
//    @Test
//    public void testSetAppThemePurpleDark() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.setAppTheme("Dark", "Purple");
//
//        verify(mockContext).setTheme(R.style.AppTheme_Purple_Dark);
//    }
//
//    @Test
//    public void testSetAppThemeGreenLight() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.setAppTheme("Light", "Green");
//
//        verify(mockContext).setTheme(R.style.AppTheme_Green_Light);
//    }
//
//    @Test
//    public void testSetAppThemeGreenDark() {
//        setPreferences("en", "Dark", "Blue");
//        controller.loadPreferences(mockContext);
//        controller.setAppTheme("Dark", "Green");
//
//        verify(mockContext).setTheme(R.style.AppTheme_Green_Dark);
//    }
//
//
//
//
//    public void setPreferences(String language, String theme, String palette) {
//        when(mockSharedPreferences.getString("language", "uk")).thenReturn(language);
//        when(mockSharedPreferences.getString("theme", "Light")).thenReturn(theme);
//        when(mockSharedPreferences.getString("palette", "Brown")).thenReturn(palette);
//    }
//}
