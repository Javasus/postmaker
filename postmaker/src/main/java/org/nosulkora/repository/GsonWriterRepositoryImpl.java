package org.nosulkora.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.nosulkora.model.Status;
import org.nosulkora.model.Writer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GsonWriterRepositoryImpl implements WriterRepository {

    private static final String FILE_PATH = "writer.json";
    Gson gson = new Gson();

    /**
     * Добавляет нового writer в файл writer.json.
     *
     * @param writer - Обьект пользователя Writer.
     * @return boolean
     */
    @Override
    public Boolean createWriter(Writer writer) {
        List<Writer> writers = getAllWriters();
        writers.add(writer);
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            gson.toJson(writers, fileWriter);
            return true;
        } catch (IOException e) {
            System.out.println("Ошбка записи.");
            return false;
        }
    }

    /**
     * Возвращает writer, если он уже существует в .writer.json
     *
     * @param firstname Имя
     * @param lastName  Фамилия
     * @return writer
     */
    @Override
    public List<Writer> getWriterByName(String firstname, String lastName) {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<List<Writer>>() {
            }.getType();
            List<Writer> writers = gson.fromJson(fileReader, userListType);
            return writers.stream()
                    .filter(writer -> writer.getFirstName().equals(firstname))
                    .filter(writer -> writer.getLastname().equals(lastName)).toList();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Возвращает список всех пользователей из файла writer.json.
     *
     * @return список пользователей
     */
    @Override
    public List<Writer> getAllWriters() {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<List<Writer>>() {
            }.getType();
            List<Writer> existing = gson.fromJson(fileReader, type);
            return existing != null ? existing : new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Возвращает writer по id из файла - writer.json.
     *
     * @param id Идентификатор writer'а
     * @return writer
     */
    @Override
    public Writer getWriterById(Long id) {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<List<Writer>>() {
            }.getType();
            List<Writer> writers = gson.fromJson(fileReader, userListType);
            Optional<Writer> writer = writers.stream().filter(wr -> wr.getId().equals(id)).findFirst();
            return writer.orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Возвращает обновленного пользователья в файле - writer.json.
     *
     * @param id       id пользователя которого хотим обновить
     * @param name     имя которое хотим присовить пользователю
     * @param LastName фамилия которую хотим присвоить пользователю
     * @return writer
     */
    public Writer updateWriter(Long id, String name, String LastName) {
        List<Writer> allWriters = getAllWriters();
        allWriters.forEach(writer -> {
            if (writer.getId().equals(id)) {
                writer.setFirstName(name);
                writer.setLastname(LastName);
            }
        });
        return addWriters(allWriters) ?
                allWriters.stream().filter(writer -> writer.getId().equals(id)).findFirst().orElse(null) :
                null;
    }

    /**
     * Возвращает удаленнного пользователя в файле writer.json.
     *
     * @param id id пользователя, которого хотим удалить
     * @return writer
     */
    public Writer deleteWriterById(Long id) {
        List<Writer> allWriters = getAllWriters();
        List<Writer> updatedWriters = allWriters.stream().peek(writer -> {
            if (writer.getId().equals(id)) {
                writer.setStatus(Status.DELETED);
            }
        }).collect(Collectors.toList());
        return addWriters(updatedWriters) ?
                updatedWriters.stream().filter(writer -> writer.getId().equals(id)).findFirst().orElse(null) :
                null;
    }

    /**
     * записывает в файл writer.json обновленную коллекцию пользователей.
     *
     * @param writers обновленная коллекция пользователей
     * @return boolean
     */
    private boolean addWriters(List<Writer> writers) {
        try (FileWriter fileWriter = new FileWriter(FILE_PATH)) {
            gson.toJson(writers, fileWriter);
            return true;
        } catch (IOException e) {
            System.out.println("Ошбка записи.");
            return false;
        }
    }
}
