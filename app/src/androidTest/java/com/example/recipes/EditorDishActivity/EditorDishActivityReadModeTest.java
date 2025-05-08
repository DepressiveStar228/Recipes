package com.example.recipes.EditorDishActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Activity.EditorDishActivity;
import com.example.recipes.Enum.IntentKeys;
import com.example.recipes.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EditorDishActivityReadModeTest {
    private EditorDishActivity activity;

    @Rule
    public ActivityScenarioRule<EditorDishActivity> activityReadModeRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), EditorDishActivity.class).putExtra(IntentKeys.DISH_ID.name(), -3L));

    @Before
    public void setUp() {
        activityReadModeRule.getScenario().onActivity(activity -> this.activity = activity);
    }

    @Test
    public void testDishNameContainer() {
        EditText dishNameEditText = activity.findViewById(R.id.nameEditTextEditAct);
        assertNotNull(dishNameEditText);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) dishNameEditText.getLayoutParams();

        assertEquals(View.VISIBLE, dishNameEditText.getVisibility());
        assertEquals(28f, dishNameEditText.getTextSize() / activity.getResources().getDisplayMetrics().scaledDensity, 0.1f);
        assertEquals(0.5f, params.horizontalBias, 0.01f);
        assertNotEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(PorterDuff.Mode.CLEAR, dishNameEditText.getBackgroundTintMode());
        assertFalse(dishNameEditText.isEnabled());
    }

    @Test
    public void testPositionContainer() {
        EditText positionEditText = activity.findViewById(R.id.portionEditTextEditAct);
        assertNotNull(positionEditText);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) positionEditText.getLayoutParams();

        assertEquals(View.VISIBLE, positionEditText.getVisibility());
        assertEquals(20f, positionEditText.getTextSize() / activity.getResources().getDisplayMetrics().scaledDensity, 0.2f);
        assertEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(PorterDuff.Mode.CLEAR, positionEditText.getBackgroundTintMode());
        assertFalse(positionEditText.isEnabled());
        assertEquals("", positionEditText.getText().toString());

        ConstraintLayout empty = activity.findViewById(R.id.portionEmpty);
        assertNotNull(empty);
        assertEquals(View.VISIBLE, empty.getVisibility());
    }

    @Test
    public void testRecipesContainer() {
        RecyclerView recipesRecyclerView = activity.findViewById(R.id.addRecipeRecycler);
        assertNotNull(recipesRecyclerView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) recipesRecyclerView.getLayoutParams();

        assertEquals(View.VISIBLE, recipesRecyclerView.getVisibility());
        assertEquals(ConstraintLayout.LayoutParams.WRAP_CONTENT, params.height);
        assertEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(0, recipesRecyclerView.getChildCount());
        assertFalse(recipesRecyclerView.isNestedScrollingEnabled());
        assertTrue(recipesRecyclerView.hasFixedSize());

        ConstraintLayout empty = activity.findViewById(R.id.recipeEmpty);
        assertNotNull(empty);
        assertEquals(View.VISIBLE, empty.getVisibility());

        ConstraintLayout buttonsRecipeContainer = activity.findViewById(R.id.buttonsRecipeContainer);
        assertNotNull(buttonsRecipeContainer);
        assertEquals(View.GONE, buttonsRecipeContainer.getVisibility());
    }

    @Test
    public void testIngredientsContainer() {
        RecyclerView recipesRecyclerView = activity.findViewById(R.id.addIngredientRecyclerViewEditAct);
        assertNotNull(recipesRecyclerView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) recipesRecyclerView.getLayoutParams();

        assertEquals(View.VISIBLE, recipesRecyclerView.getVisibility());
        assertEquals(ConstraintLayout.LayoutParams.WRAP_CONTENT, params.height);
        assertEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(0, recipesRecyclerView.getChildCount());
        assertFalse(recipesRecyclerView.isNestedScrollingEnabled());
        assertTrue(recipesRecyclerView.hasFixedSize());

        ConstraintLayout empty = activity.findViewById(R.id.ingredientEmpty);
        assertNotNull(empty);
        assertEquals(View.VISIBLE, empty.getVisibility());

        AppCompatImageView addIngredient = activity.findViewById(R.id.addIngredientButtonEditAct);
        assertNotNull(addIngredient);
        assertEquals(View.GONE, addIngredient.getVisibility());
    }

    @Test
    public void testCollectionsContainer() {
        RecyclerView recipesRecyclerView = activity.findViewById(R.id.addCollectionRecyclerView);
        assertNotNull(recipesRecyclerView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) recipesRecyclerView.getLayoutParams();

        assertEquals(View.VISIBLE, recipesRecyclerView.getVisibility());
        assertEquals(ConstraintLayout.LayoutParams.WRAP_CONTENT, params.height);
        assertEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(0, recipesRecyclerView.getChildCount());
        assertFalse(recipesRecyclerView.isNestedScrollingEnabled());
        assertTrue(recipesRecyclerView.hasFixedSize());

        ConstraintLayout empty = activity.findViewById(R.id.collectionEmpty);
        assertNotNull(empty);
        assertEquals(View.VISIBLE, empty.getVisibility());

        AppCompatImageView addCollection = activity.findViewById(R.id.addCollectionButton);
        assertNotNull(addCollection);
        assertEquals(View.GONE, addCollection.getVisibility());
    }

    @Test
    public void testActionButton() {
        Button actionButton = activity.findViewById(R.id.editButton);
        assertNotNull(actionButton);
        assertEquals(View.GONE, actionButton.getVisibility());
    }

    @Test
    public void testOption() {
        AppCompatImageView optionButton = activity.findViewById(R.id.imageSettingImageView);
        assertNotNull(optionButton);
        assertEquals(View.VISIBLE, optionButton.getVisibility());

        DrawerLayout drawerLayout = activity.findViewById(R.id.readDrawerLayout);
        assertNotNull(drawerLayout);
        assertEquals(DrawerLayout.LOCK_MODE_UNLOCKED, drawerLayout.getDrawerLockMode(Gravity.END));
    }
}
