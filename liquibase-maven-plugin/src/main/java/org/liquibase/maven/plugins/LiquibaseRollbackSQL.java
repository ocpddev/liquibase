package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;

/**
 * <p>Generates the SQL that is required to rollback the database using one or more of the specified
 * attributes 'rollbackCount', 'rollbackTag' and/or 'rollbackDate'</p>
 *
 * @description Liquibase RollbackSQL Maven plugin
 * @goal rollbackSQL
 */
public class LiquibaseRollbackSQL extends LiquibaseRollback {

    /**
     * The file to output the Rollback SQL script to, if it exists it will be
     * overwritten.
     *
     * @parameter property="liquibase.migrationSqlOutputFile"
     *            default-value=
     *            "${project.build.directory}/liquibase/migrate.sql"
     */
    @PropertyElement
    protected File migrationSqlOutputFile;

    /** The writer for writing the migration SQL. */
    private Writer outputWriter;

    @Override
    protected Liquibase createLiquibase(Database db)
            throws MojoExecutionException {
        Liquibase liquibase = super.createLiquibase(db);

        // Setup the output file writer
        try {
            if (!migrationSqlOutputFile.exists()) {
                // Ensure the parent directories exist
                migrationSqlOutputFile.getParentFile().mkdirs();
                // Create the actual file
                if (!migrationSqlOutputFile.createNewFile()) {
                    throw new MojoExecutionException(
                            "Cannot create the migration SQL file; "
                                    + migrationSqlOutputFile.getAbsolutePath());
                }
            }
            outputWriter = getOutputWriter(migrationSqlOutputFile);
        } catch (IOException e) {
            getLog().error(e);
            throw new MojoExecutionException(
                    "Failed to create SQL output writer", e);
        }
        getLog().info(
                "Output SQL Migration File: "
                        + migrationSqlOutputFile.getAbsolutePath());
        return liquibase;
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(
                indent + "migrationSQLOutputFile: " + migrationSqlOutputFile);
    }

    @Override
    protected void cleanup(Database db) {
        super.cleanup(db);
        if (outputWriter != null) {
            try {
                outputWriter.close();
            } catch (IOException e) {
                getLog().error(e);
            }
        }
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
            throws LiquibaseException {
        switch (type) {
        case COUNT: {
            liquibase.rollback(rollbackCount, rollbackScript,new Contexts(contexts), new LabelExpression(getLabelFilter()), outputWriter);
            break;
        }
        case DATE: {
            try {
                liquibase.rollback(parseDate(rollbackDate), rollbackScript,new Contexts(contexts), new LabelExpression(getLabelFilter()),
                        outputWriter);
            } catch (ParseException e) {
                String message = "Error parsing rollbackDate: "
                        + e.getMessage();
                throw new LiquibaseException(message, e);
            }
            break;
        }
        case TAG: {
            liquibase.rollback(rollbackTag, rollbackScript,new Contexts(contexts), new LabelExpression(getLabelFilter()), outputWriter);
            break;
        }
        default: {
            throw new IllegalStateException("Unexpected rollback type, " + type);
        }
        }
    }
}
