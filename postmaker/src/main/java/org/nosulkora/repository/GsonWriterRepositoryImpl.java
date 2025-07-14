package org.nosulkora.repository;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.nosulkora.model.Writer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GsonWriterRepositoryImpl implements WriterRepository {

    private static String FILE_PATH = "writer.json";
    Gson gson = new Gson();

    /**
     * Добавляет нового writer в файл writer.json.
     *
     * @param writer - Обьект пользователя Writer.
     * @return boolean
     */
    public Boolean addWriter(Writer writer) {
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
    public Optional<Writer> getWriterByName(String firstname, String lastName) {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<List<Writer>>() {
            }.getType();
            List<Writer> writers = gson.fromJson(fileReader, userListType);

            return writers.stream()
                    .filter(writer -> writer.getFirstName().equals(firstname))
                    .filter(writer -> writer.getLastname().equals(lastName)).findFirst();

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Возвращает список всех пользователей из файла writer.json.
     *
     * @return список пользователей
     */
    public List<Writer> getAllWriters() {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<List<Writer>>() {}.getType();
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
    public Optional<Writer> getWriterById(Long id) {
        try (FileReader fileReader = new FileReader(FILE_PATH)) {
            Type userListType = new TypeToken<List<Writer>>() {
            }.getType();
            List<Writer> writers = gson.fromJson(fileReader, userListType);

            return writers.stream().filter(writer -> writer.getId().equals(id)).findFirst();
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
