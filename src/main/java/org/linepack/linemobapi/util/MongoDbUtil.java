/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.linepack.linemobapi.util;

import com.google.gson.Gson;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.linepack.linemobapi.service.AbstractFacade;

/**
 *
 * @author Leandro
 * @param <T>
 */
public class MongoDbUtil<T> {

    private final String dbName;
    private final Class<T> entityClass;

    public MongoDbUtil(String dbName, Class<T> entityClass) {
        this.dbName = dbName;
        this.entityClass = entityClass;
    }

    public MongoDatabase getMongoDatabase() throws UnknownHostException {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db;
        db = mongoClient.getDatabase(dbName);
        return db;
    }

    public MongoCollection<Document> getMongoCollection() throws UnknownHostException {
        return this.getMongoDatabase().getCollection(entityClass.getSimpleName());
    }

    public Document getDocumentFromEntity(T entity) throws IllegalArgumentException, IllegalAccessException {
        Document document = new Document();
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            document.append(field.getName(), field.get(entity));
        }
        return document;
    }

    public List<T> getListFromIterable(FindIterable iterable) {
        final List<T> list = new ArrayList<>();
        try {
            iterable.forEach(new Block<Document>() {
                @Override
                public void apply(Document document) {
                    try {
                        Gson gson = new Gson();
                        T object = gson.fromJson(document.toJson(), entityClass);
                        ObjectId objId = document.getObjectId("_id");
                        Field idField = object.getClass().getSuperclass().getDeclaredField("id");
                        idField.setAccessible(true);
                        idField.set(object, objId.toString());
                        list.add((T) object);
                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                        Logger.getLogger(AbstractFacade.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
        } catch (NullPointerException npe) {

        }
        return list;
    }
}