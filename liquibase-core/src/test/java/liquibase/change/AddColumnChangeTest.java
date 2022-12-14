package liquibase.change;

import liquibase.change.core.AddColumnChange;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MockDatabase;
import liquibase.exception.RollbackImpossibleException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.DropColumnStatement;
import liquibase.statement.core.ReorganizeTableStatement;
import org.junit.Assert;
import org.junit.Test;

public class AddColumnChangeTest {

    @Test
    public void generateStatements_multipleColumns() {
        AddColumnChange change = new AddColumnChange();
        AddColumnConfig column1 = new AddColumnConfig();
        column1.setName("column1");
        column1.setType("INT");
        change.addColumn(column1);
        AddColumnConfig column2 = new AddColumnConfig();
        column2.setName("column2");
        column2.setType("INT");
        change.addColumn(column2);

        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        Assert.assertEquals(1, statements.length);
        Assert.assertTrue(statements[0] instanceof AddColumnStatement);
        AddColumnStatement stmt = (AddColumnStatement)statements[0];
        Assert.assertTrue(stmt.isMultiple());
        Assert.assertEquals(2, stmt.getColumns().size());
    }

    @Test
    public void generateStatements_DB2_multipleColumns_single_reorg() {
        AddColumnChange change = new AddColumnChange();
        AddColumnConfig column1 = new AddColumnConfig();
        column1.setName("column1");
        column1.setType("INT");
        change.addColumn(column1);
        AddColumnConfig column2 = new AddColumnConfig();
        column2.setName("column2");
        column2.setType("INT");
        change.addColumn(column2);

        SqlStatement[] statements = change.generateStatements(new DB2Database());
        Assert.assertEquals(2, statements.length);
        Assert.assertTrue(statements[0] instanceof AddColumnStatement);
        AddColumnStatement stmt = (AddColumnStatement)statements[0];
        Assert.assertTrue(stmt.isMultiple());
        Assert.assertEquals(2, stmt.getColumns().size());
        Assert.assertTrue(statements[1] instanceof ReorganizeTableStatement);

    }

    @Test
    public void generateStatements_singleColumn_uniqueConstraintName() {
        String myUniqueConstraintName  = "my_unique_constraint";

        AddColumnChange change = new AddColumnChange();
        change.setTableName("my_table");
        AddColumnConfig column = new AddColumnConfig();
        column.setName("column1");
        column.setType("integer");
        ConstraintsConfig constraintsConfig = new ConstraintsConfig();
        constraintsConfig.setUnique(true);
        constraintsConfig.setUniqueConstraintName(myUniqueConstraintName);
        column.setConstraints(constraintsConfig);
        change.addColumn(column);

        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        Assert.assertEquals(1, statements.length);
        Assert.assertTrue(statements[0] instanceof AddColumnStatement);
        AddColumnStatement stmt = (AddColumnStatement)statements[0];
        Assert.assertEquals(myUniqueConstraintName, stmt.getUniqueStatementName());
    }

    @Test
    public void generateStatements_singleColumn_null_uniqueConstraintName() {
        AddColumnChange change = new AddColumnChange();
        change.setTableName("my_table");
        AddColumnConfig column = new AddColumnConfig();
        column.setName("column1");
        column.setType("integer");
        ConstraintsConfig constraintsConfig = new ConstraintsConfig();
        constraintsConfig.setUnique(true);
        column.setConstraints(constraintsConfig);
        change.addColumn(column);

        SqlStatement[] statements = change.generateStatements(new MockDatabase());
        Assert.assertEquals(1, statements.length);
        Assert.assertTrue(statements[0] instanceof AddColumnStatement);
        AddColumnStatement stmt = (AddColumnStatement)statements[0];
        Assert.assertNull(stmt.getUniqueStatementName());
    }

    @Test
    public void generateRollbackStatements_catalog_schema_table() throws RollbackImpossibleException {
        AddColumnChange change = new AddColumnChange();
        change.setCatalogName("catalog1");
        change.setSchemaName("schema1");
        change.setTableName("table1");

        SqlStatement[] statements = change.generateRollbackStatements(new MockDatabase());
        Assert.assertEquals(1, statements.length);
        Assert.assertTrue(statements[0] instanceof DropColumnStatement);
        DropColumnStatement dropStmt = (DropColumnStatement)statements[0];
        Assert.assertEquals("catalog1", dropStmt.getCatalogName());
        Assert.assertEquals("schema1", dropStmt.getSchemaName());
        Assert.assertEquals("table1", dropStmt.getTableName());
    }
}
