package it.appventurers.taskflow.ui.viewmodel.data;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import it.appventurers.taskflow.data.repository.data.DataRepository;
import it.appventurers.taskflow.model.Daily;
import it.appventurers.taskflow.model.Habit;
import it.appventurers.taskflow.model.Result;
import it.appventurers.taskflow.model.ToDo;
import it.appventurers.taskflow.model.User;

public class DataViewModel extends ViewModel {

    private final DataRepository dataRepository;
    private MutableLiveData<Result> data;
    private MutableLiveData<Result> habitData;
    private MutableLiveData<User> userInfo;


    public DataViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
        data = new MutableLiveData<>();
        userInfo = new MutableLiveData<>();
    }

    public void saveUser(User user) {
        dataRepository.saveUser(user);
        setData();
    }

    public void getUserInfo(User user) {
        dataRepository.getUserInfo(user);
        setUserInfo();
    }

    public void updateUser(User user) {
        dataRepository.updateUser(user);
        setUserInfo();
    }

    public void deleteUser(User user) {
        dataRepository.deleteUser(user);
        setData();
    }

    public void saveHabit(User user, Habit habit) {
        dataRepository.saveHabit(user, habit);
        setData();
    }

    public void getAllHabit(User user) {
        dataRepository.getAllHabit(user);
        setData();
    }

    public void updateHabit(User user, Habit habit) {
        dataRepository.updateHabit(user, habit);
        setData();
    }

    public void deleteHabit(User user, Habit habit) {
        dataRepository.deleteHabit(user, habit);
        setData();
    }

    public void saveDaily(User user, Daily daily) {
        dataRepository.saveDaily(user, daily);
        setData();
    }

    public void getAllDaily(User user) {
        dataRepository.getAllDaily(user);
        setData();
    }

    public void updateDaily(User user, Daily daily) {
        dataRepository.updateDaily(user, daily);
        setData();
    }

    public void deleteDaily(User user, Daily daily) {
        dataRepository.deleteDaily(user, daily);
        setData();
    }

    public void saveToDo(User user, ToDo toDo) {
        dataRepository.saveToDo(user, toDo);
        setData();
    }

    public void getAllToDo(User user) {
        dataRepository.getAllToDo(user);
        setData();
    }

    public void updateToDo(User user, ToDo toDo) {
        dataRepository.updateToDo(user, toDo);
        setData();
    }

    public void deleteToDo(User user, ToDo toDo) {
        dataRepository.deleteToDo(user, toDo);
        setData();
    }

    public MutableLiveData<Result> getData() {
        return data;
    }

    public MutableLiveData<User> getUserInfo() {
        return userInfo;
    }

    private void setData() {
        data = dataRepository.getData();
    }

    private void setUserInfo() {
        userInfo = dataRepository.getUserInfo();
    }
}