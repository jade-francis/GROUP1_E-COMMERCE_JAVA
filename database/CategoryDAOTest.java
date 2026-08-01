import java.util.List;

/**
 * Quick manual test for CategoryDAO — walks the full CRUD:
 * add, read (all / by id / by name), duplicate-name rejection, update, delete.
 *
 * Self-cleaning: uses a unique timestamp-based name so it never collides with
 * the seed categories or the UNIQUE(name) constraint, and deletes the row it
 * created at the end. Deleting a category is safe here because the test
 * category has no products (and products.category_id is ON DELETE SET NULL
 * anyway, so it wouldn't delete real products).
 */
public class CategoryDAOTest {
    public static void main(String[] args) {
        CategoryDAO categoryDAO = new CategoryDAOImpl();

        String name = "TestCat-" + System.currentTimeMillis();

        System.out.println("=== All categories (seed data) ===");
        for (Category c : categoryDAO.getAllCategories()) {
            System.out.println("  " + c);
        }

        // --- Add ---
        System.out.println("\n=== Add category '" + name + "' ===");
        Category added = categoryDAO.addCategory(name, "A throwaway test category");
        System.out.println("Added: " + added);
        if (added == null) {
            System.out.println("Add failed — aborting.");
            return;
        }
        int id = added.getId();

        // --- Read by id / by name ---
        System.out.println("\n=== Read back ===");
        Category byId = categoryDAO.getCategoryById(id);
        Category byName = categoryDAO.getCategoryByName(name);
        System.out.println("getCategoryById(" + id + "): " + byId);
        System.out.println("getCategoryByName(\"" + name + "\"): " + byName);
        System.out.println("Both point to same category? " +
            (byId != null && byName != null && byId.getId() == byName.getId()));

        // --- Duplicate name should be rejected (UNIQUE constraint) ---
        System.out.println("\n=== Add duplicate name (expect null) ===");
        Category dup = categoryDAO.addCategory(name, "different description, same name");
        System.out.println("Duplicate result: " + dup);

        // --- Update ---
        System.out.println("\n=== Update ===");
        String newName = name + "-renamed";
        boolean updated = categoryDAO.updateCategory(id, newName, "updated description");
        System.out.println("updateCategory: " + updated);
        System.out.println("After update: " + categoryDAO.getCategoryById(id));

        // --- Delete ---
        System.out.println("\n=== Delete ===");
        boolean deleted = categoryDAO.deleteCategory(id);
        System.out.println("deleteCategory: " + deleted);
        System.out.println("Fetch after delete (expect null): " + categoryDAO.getCategoryById(id));

        // --- Confirm seed categories are untouched ---
        System.out.println("\n=== Categories after test (should match seed) ===");
        List<Category> remaining = categoryDAO.getAllCategories();
        for (Category c : remaining) {
            System.out.println("  " + c);
        }
    }
}
