package com.javeriana.sdp.controllers.courses;

import com.javeriana.sdp.sql.SQLProvider;
import com.javeriana.sdp.sql.SQLUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.sql.Connection;

/**
 * Created by Sebastian on 24/11/18
 * Email: Juan.2114@hotmail.com
 * Email: Juan2114@javerianacali.edu.co
 */
@Controller
@RequestMapping("/curso")
public class CourseController {

    /**
     * Represents the button type tag used for the front end to identify whether or not
     * the admin is editing
     */
    private static final String BUTTON_TYPE = "buttonType";

    /**
     * Represents a flag indicating that the website is rendering the edit state of the page
     */
    private static final int EDIT_STATE = 1;

    /**
     * Represents a flag indicating that the website is rendering the save state of the page
     */
    private static final int SAVE_STATE = 0;

    /**
     * Search engine provider.
     * This mapping is used to automate the search engine
     * @param id    the id provided by the search engine automatic response
     **/
    @RequestMapping(method = RequestMethod.GET, params = {"id"})
    public ModelAndView renderSearchProvider(@RequestParam(value = "id") int id) {
        return defaultRender(id);
    }

    /**
     * Represents the edit action
     * @param courseId  the course id associated to this controller
     * @return  the edit view of this controller
     */
    @RequestMapping(method = RequestMethod.POST, params = {"editFlag", "courseId"})
    public ModelAndView editAction(@RequestParam(value = "courseId") long courseId) {
        /** Requesting to edit the content **/
        if (!SQLProvider.getSingleton().hasFreeConnections()) {
            // Notice this shouldn't happen because we won't have too many requests
            return new ModelAndView("index");
        }
        // Take a free connection
        final Connection connection = SQLProvider.getSingleton().take();
        // This can only happen in 20 years
        if (courseId >= Integer.MAX_VALUE) {
            throw new RuntimeException("ID Exceeded the integer max value! Clear the BD :)");
        }
        // Poll the content of the web page
        String content = SQLUtils.pollContent((int) courseId, connection);
        // Dispose the connection
        SQLProvider.getSingleton().dispose(connection);
        return new ModelAndView("course").addObject("editableContent", content).addObject(BUTTON_TYPE, SAVE_STATE).addObject("courseId", courseId);
    }

    /**
     * Represents the store action
     * @param modification  the modified content of the course
     * @param courseId  the course id
     * @return  the deafult view of this controller
     */
    @RequestMapping(method = RequestMethod.POST, params = {"storeFlag", "courseId"})
    public ModelAndView storeAction(@RequestParam(value = "modifiedContent") String modification, @RequestParam(value = "courseId") long courseId) {
        /** Requesting to persist the content **/
        if (!SQLProvider.getSingleton().hasFreeConnections()) {
            // Notice this shouldn't happen because we won't have too many requests
            return new ModelAndView("index");
        }
        // Take a free connection
        final Connection connection = SQLProvider.getSingleton().take();
        // Execute all the queries
        SQLUtils.executeQueries(connection, "UPDATE Content SET data = '" + modification + "' WHERE categoryId = " + courseId);
        // Free the connection
        SQLProvider.getSingleton().dispose(connection);
        // Render the changes
        return defaultRender(courseId);
    }

    /**
     * Default view of this controller based on the name of the course
     * rather than the id
     * @param name  the name of the course
     * @return  the default view
     */
    @RequestMapping(method = RequestMethod.GET, params = {"nombre"})
    public ModelAndView defaultRender(@RequestParam(value = "nombre") String name) {
        /** Requesting to persist the content **/
        if (!SQLProvider.getSingleton().hasFreeConnections()) {
            // Notice this shouldn't happen because we won't have too many requests
            return new ModelAndView("index");
        }
        // Format the name for the DB
        name = name.toLowerCase();
        // Take a free connection
        final Connection connection = SQLProvider.getSingleton().take();
        // Find the category id
        long id = (Long) SQLUtils.getDataFromQuery(connection, "SELECT id FROM ContentCategory WHERE name='" + name + "'", "id").get(0)[0];
        // Free the connection
        SQLProvider.getSingleton().dispose(connection);
        // Render the changes
        return defaultRender(id);
    }

    /**
     * Default view of this controller based on the id of the course
     * @param id    the id of the course
     * @return  the default view
     */
    public ModelAndView defaultRender(final long id) {
        /** Requesting to persist the content **/
        if (!SQLProvider.getSingleton().hasFreeConnections()) {
            // Notice this shouldn't happen because we won't have too many requests
            return new ModelAndView("index");
        }
        // This can only happen in 20 years
        if (id >= Integer.MAX_VALUE) {
            throw new RuntimeException("ID Exceeded the integer max value! Clear the BD :)");
        }
        // Take a free connection
        final Connection connection = SQLProvider.getSingleton().take();
        // Poll the content
        String content = SQLUtils.pollContent((int) id, connection);
        // Display all the content with the corresponding new line of html
        content = content.replaceAll("\n", "<br/>");
        // Free the connection
        SQLProvider.getSingleton().dispose(connection);
        // Render the changes
        return new ModelAndView("course").addObject("content", content).addObject(BUTTON_TYPE, EDIT_STATE).addObject("courseId", id);
    }

}
