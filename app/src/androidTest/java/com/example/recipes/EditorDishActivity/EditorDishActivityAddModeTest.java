package com.example.recipes.EditorDishActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.recipes.Activity.EditorDishActivity;
import com.example.recipes.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class EditorDishActivityAddModeTest {
    private EditorDishActivity activity;

    @Rule
    public ActivityScenarioRule<EditorDishActivity> activityVoidRule =
            new ActivityScenarioRule<>(EditorDishActivity.class);

    @Before
    public void setUp() {
        activityVoidRule.getScenario().onActivity(activity -> this.activity = activity);
    }

    @Test
    public void testDishNameContainer() {
        EditText dishNameEditText = activity.findViewById(R.id.nameEditTextEditAct);
        assertNotNull(dishNameEditText);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) dishNameEditText.getLayoutParams();
        assertEquals(View.VISIBLE, dishNameEditText.getVisibility());
        assertEquals(20f, dishNameEditText.getTextSize() / activity.getResources().getDisplayMetrics().scaledDensity, 0.2f);
        assertEquals(0f, params.horizontalBias, 0.01f);
        assertEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertNull(dishNameEditText.getBackgroundTintMode());
        assertTrue(dishNameEditText.isEnabled());
        assertEquals("", dishNameEditText.getText().toString());

        TextView containerNameTextView = activity.findViewById(R.id.nameTextViewContainer);
        assertNotNull(containerNameTextView);
        assertEquals(View.VISIBLE, containerNameTextView.getVisibility());

        ConstraintLayout container = activity.findViewById(R.id.nameDishContainerWithBorder);
        assertNotNull(container);
        assertEquals(255, container.getBackground().getAlpha());
    }

    @Test
    public void testPositionContainer() {
        EditText positionEditText = activity.findViewById(R.id.portionEditTextEditAct);
        assertNotNull(positionEditText);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) positionEditText.getLayoutParams();

        assertEquals(View.VISIBLE, positionEditText.getVisibility());
        assertEquals(20f, positionEditText.getTextSize() / activity.getResources().getDisplayMetrics().scaledDensity, 0.2f);
        assertEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertNull(positionEditText.getBackgroundTintMode());
        assertTrue(positionEditText.isEnabled());
        assertEquals("", positionEditText.getText().toString());

        ConstraintLayout empty = activity.findViewById(R.id.portionEmpty);
        assertNotNull(empty);
        assertEquals(View.GONE, empty.getVisibility());
    }

    @Test
    public void testRecipesContainer() {
        RecyclerView recipesRecyclerView = activity.findViewById(R.id.addRecipeRecycler);
        assertNotNull(recipesRecyclerView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) recipesRecyclerView.getLayoutParams();

        assertEquals(View.VISIBLE, recipesRecyclerView.getVisibility());
        assertEquals(400, params.height / activity.getResources().getDisplayMetrics().density, 1f);
        assertEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(0, recipesRecyclerView.getChildCount());
        assertTrue(recipesRecyclerView.isNestedScrollingEnabled());
        assertTrue(recipesRecyclerView.hasFixedSize());

        ConstraintLayout empty = activity.findViewById(R.id.recipeEmpty);
        assertNotNull(empty);
        assertEquals(View.VISIBLE, empty.getVisibility());

        ConstraintLayout buttonsRecipeContainer = activity.findViewById(R.id.buttonsRecipeContainer);
        assertNotNull(buttonsRecipeContainer);
        assertEquals(View.VISIBLE, buttonsRecipeContainer.getVisibility());
    }

    @Test
    public void testIngredientsContainer() {
        RecyclerView recipesRecyclerView = activity.findViewById(R.id.addIngredientRecyclerViewEditAct);
        assertNotNull(recipesRecyclerView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) recipesRecyclerView.getLayoutParams();

        assertEquals(View.VISIBLE, recipesRecyclerView.getVisibility());
        assertEquals(300, params.height / activity.getResources().getDisplayMetrics().density, 1f);
        assertEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(0, recipesRecyclerView.getChildCount());
        assertTrue(recipesRecyclerView.isNestedScrollingEnabled());
        assertTrue(recipesRecyclerView.hasFixedSize());

        ConstraintLayout empty = activity.findViewById(R.id.ingredientEmpty);
        assertNotNull(empty);
        assertEquals(View.VISIBLE, empty.getVisibility());

        AppCompatImageView addIngredient = activity.findViewById(R.id.addIngredientButtonEditAct);
        assertNotNull(addIngredient);
        assertEquals(View.VISIBLE, addIngredient.getVisibility());
    }

    @Test
    public void testCollectionsContainer() {
        RecyclerView recipesRecyclerView = activity.findViewById(R.id.addCollectionRecyclerView);
        assertNotNull(recipesRecyclerView);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) recipesRecyclerView.getLayoutParams();

        assertEquals(View.VISIBLE, recipesRecyclerView.getVisibility());
        assertEquals(200, params.height / activity.getResources().getDisplayMetrics().density, 1f);
        assertEquals(ConstraintLayout.LayoutParams.MATCH_PARENT, params.width);
        assertEquals(1, recipesRecyclerView.getChildCount());
        assertTrue(recipesRecyclerView.isNestedScrollingEnabled());
        assertTrue(recipesRecyclerView.hasFixedSize());

        ConstraintLayout empty = activity.findViewById(R.id.collectionEmpty);
        assertNotNull(empty);
        assertEquals(View.GONE, empty.getVisibility());

        AppCompatImageView addCollection = activity.findViewById(R.id.addCollectionButton);
        assertNotNull(addCollection);
        assertEquals(View.VISIBLE, addCollection.getVisibility());
    }

    @Test
    public void testActionButton() {
        Button actionButton = activity.findViewById(R.id.editButton);
        assertNotNull(actionButton);
        assertEquals(View.VISIBLE, actionButton.getVisibility());
        assertEquals(activity.getString(R.string.button_add), actionButton.getText().toString());
    }

    @Test
    public void testOption() {
        AppCompatImageView optionButton = activity.findViewById(R.id.imageSettingImageView);
        assertNotNull(optionButton);
        assertEquals(View.GONE, optionButton.getVisibility());

        DrawerLayout drawerLayout = activity.findViewById(R.id.readDrawerLayout);
        assertNotNull(drawerLayout);
        assertEquals(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, drawerLayout.getDrawerLockMode(Gravity.END));
    }
}
