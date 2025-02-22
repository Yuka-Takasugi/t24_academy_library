package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.controller.StockController;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.service.DailyAvailable;

//import java.util.Map;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public List<CalendarDto> generateValues(Integer year, Integer month, Integer daysInMonth) {

        List<BookMst> books = this.bookMstRepository.findAll();
        // List<Object[]> countBylendableBooks =
        // this.stockRepository.countByLendableBook();

        List<CalendarDto> values = new ArrayList<>();

        for (int bookList = 0; bookList < books.size(); bookList++) {
            BookMst book = books.get(bookList);

            List<Stock> stockCount = this.stockRepository.findByBookMstIdAndStatus(book.getId(),
                    Constants.STOCK_AVAILABLE);
            // CountにIdに紐づく利用可能在庫数を取得

            CalendarDto calendarDto = new CalendarDto();
            calendarDto.setTitle(book.getTitle());
            calendarDto.setCount(stockCount.size());// データの数だけ

            List<DailyAvailable> dailyAvailableList = new ArrayList<>();

            // 日付ごとの在庫数
            for (int day = 1; day <= daysInMonth; day++) {
                Calendar cl = Calendar.getInstance();
                cl.set(Calendar.YEAR, year);
                cl.set(Calendar.MONTH, month - 1);
                cl.set(Calendar.DATE, day);
                Date date = new Date();
                date = cl.getTime();

                DailyAvailable dailyAvailable = new DailyAvailable();
                List<Object[]> stockList = stockRepository.calendar(book.getId(), date);

                stockList.size();
                dailyAvailable.setLendable_book(stockList.size());
                dailyAvailable.setExpectedRentalOn(date);
                dailyAvailable.setStockId(stockList.isEmpty() ? null : stockList.get(0)[0].toString());

                dailyAvailableList.add(dailyAvailable);
            }
            calendarDto.setDailyDetail(dailyAvailableList);
            values.add(calendarDto);
        }
        return values;
    }
    /*
     * //Stock calendarで使う書籍名のList(SQLで取得したレコード)
     * 
     * @Transactional
     * public List<String> getAllTitles(){
     * List<String>titles = this.bookMstRepository.findAllTitles();
     * return titles;
     * }
     */
}

// FIXME ここで各書籍毎の日々の在庫を生成する処理を実装する
// FIXME ランダムに値を返却するサンプルを実装している
// String[] stockNum = {"1", "2", "3", "4", "×"};
// Random rnd = new Random();
// List<String> values = new ArrayList<>();
// values.add("スッキリわかるJava入門 第4版"); // 対象の書籍名
// values.add("10"); // 対象書籍の在庫総数

// for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
// int index = rnd.nextInt(stockNum.length);
// values.add(stockNum[index]);
// }
// return values;
// }
