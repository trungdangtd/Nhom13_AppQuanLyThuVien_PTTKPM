package edu.huflit.myapplication4;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.huflit.myapplication4.Entity.Account;
import edu.huflit.myapplication4.Entity.Book;
import edu.huflit.myapplication4.Entity.Copy;
import edu.huflit.myapplication4.Entity.Genre;
import edu.huflit.myapplication4.Entity.LibraryCard;
import edu.huflit.myapplication4.Entity.Loan;
import edu.huflit.myapplication4.Fragment.AccountFragment;

public class BookstoreProjectDatabase {
    private static final String BOOK = "Book"; // Tên bảng
    private static final String COPY = "Copy"; // Tên bảng
    private static final String LOAN = "Loan"; // Tên bảng
    private static final String LIBRARYCARD = "LibraryCard"; // Tên bảng
    private static final String GENRE = "Genre"; // Tên bảng
    private static final String ACCOUNT = "Account"; // Tên bảng
    public static ArrayList<Book> books; // sách
    public static ArrayList<Genre> genres; // sách
    public static ArrayList<String> bookName; // từ khóa gợi ý khi tìm kiếm
    public static ArrayList<Copy> copies; // bản sao của sách
    public static ArrayList<Account> accounts;
    public static ArrayList<String> accNames;
    public static ArrayList<LibraryCard> libraryCards;
    public static ArrayList<Loan> loans;
    public static Account accountInfo;
    public static LibraryCard libraryCard;
    @SuppressLint("StaticFieldLeak")
    static FirebaseFirestore database;
    static CollectionReference bookCollectionRef, copyCollectionRef, loanCollectionRef, libraryCardCollectionRef, genreCollectionRef, accountCollectionRef; // Các collection trong database Bookstore

    // Kết nối với database và bảng
    public static void ConnectToFirestoreDB()
    {
        database = FirebaseFirestore.getInstance();
        System.out.println("Connected to database successfully!");

        bookCollectionRef = database.collection(BOOK);
        System.out.println("Connected to Book table successfully!");

        copyCollectionRef = database.collection(COPY);
        System.out.println("Connected to Copy table successfully!");

        loanCollectionRef = database.collection(LOAN);
        System.out.println("Connect to Loan table successfully!");

        libraryCardCollectionRef = database.collection(LIBRARYCARD);
        System.out.println("Connected to LibraryCard table successfully!");

        genreCollectionRef = database.collection(GENRE);
        System.out.println("Connected to Genre table successfully!");

        accountCollectionRef = database.collection(ACCOUNT);
        System.out.println("Connected to Account table successfully!");
    }

    public static ArrayList<Book> booksAfterSorted;
    // Tải thể loại
    public static void LoadGenre()
    {
        genres = new ArrayList<>();
        Task<QuerySnapshot> genreIds = genreCollectionRef.get();
        while (true)
        {
            if(genreIds.isSuccessful()) {
                for (DocumentSnapshot genreId : genreIds.getResult())
                    genres.add(new Genre(genreId.getId(), genreId.getString("Name")));
                break;
            }
        }
    }

    public static ArrayList<Book> GetBooks() { return books; }
    public static ArrayList<Copy> GetCopies() { return copies; }

    public static ArrayList<String> bookIDs;

    // Tải sách
    public static void LoadBooks()
    {
        books = new ArrayList<>();
        bookName = new ArrayList<>();
        bookIDs = new ArrayList<>();
        String content = "";
        Task<QuerySnapshot> bookIds = bookCollectionRef.get();
        while(true) {
            if (bookIds.isSuccessful()) {
                for (DocumentSnapshot id : bookIds.getResult()) {
                    content = "";
                    for(String arCon : (List<String>)id.get("Content"))
                    {
                        content += arCon + "\n";
                    }
                    books.add(new Book(id.getId(),
                            id.getString("Name"),
                            id.getString("Author"),
                            id.getString("Genre"),
                            content,
                            id.getString("YearPublished"),
                            id.getString("Publisher"),
                            id.getString("URL")));
                    bookName.add(id.getString("Name"));
                    bookIDs.add(id.getId());
                    System.out.println("Book name " + id.getId() + " : " + id.getString("Name"));
                }
                break;
            }
        }
    }


    // Tải sách theo thể loại
    public static void LoadBooksWithGenre(@NonNull String genreName)
    {
        books = new ArrayList<>();

        String content = "";
        Task<QuerySnapshot> bookIds = bookCollectionRef.whereEqualTo("Genre", genreName).get();
        while(true) {
            if (bookIds.isSuccessful()) {
                for (DocumentSnapshot id : bookIds.getResult()) {
                    content = "";
                    for (String arCon : (List<String>) id.get("Content")) {
                        content += arCon + "\n";
                    }
                    books.add(new Book(id.getId(),
                            id.getString("Name"),
                            id.getString("Author"),
                            id.getString("Genre"),
                            content,
                            id.getString("YearPublished"),
                            id.getString("Publisher"),
                            id.getString("URL")));
                    System.out.println("Book name " + id.getId() + " : " + id.getString("Name"));
                }
                break;
            }
        }

        copies = new ArrayList<>();

        for(Book book : books)
        {
            Task<QuerySnapshot> copyIds = copyCollectionRef.document(book.getId()).collection("BookCopy").get();
            while(true) {
                if (copyIds.isSuccessful()) {
                    for (DocumentSnapshot copy : copyIds.getResult()) {
                        copies.add(new Copy(copy.getId(),
                                book.getId(),
                                copy.getString("Status"),
                                copy.getString("Notes")));
                        System.out.println("Copy id " + copy.getId() + ", book id " + book.getId());
                    }
                    break;
                }
            }
        }
    }

    // Login
    public static void SearchAccount(@NonNull String account, @NonNull String password, @NonNull View view)
    {
        accountInfo = new Account();
        Task<QuerySnapshot> accountName = accountCollectionRef.whereEqualTo("Account", account).get();
        while(true)
        {
            if(accountName.isSuccessful())
            {
                if(accountName.getResult().size() != 0) {
                    for (DocumentSnapshot name : accountName.getResult())
                        if (name.getString("Password").equals(password)) {
                            accountInfo = new Account(name.getString("Account"), name.getString("Password"), name.getString("Role"));
                            if(accountInfo.getRole().equals("Sinh viên"))
                            {
                                if(!name.getBoolean("isLogin"))
                                    accountCollectionRef.document(account).update("isLogin", true);
                                else
                                {
                                    accountInfo = new Account();
                                    Snackbar.make(view, "Tài khoản hoặc mật khẩu bị sai", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                    return;
                                }
                            }
                        }
                    if(TextUtils.isEmpty(accountInfo.getAccount()))
                    {
                        Snackbar.make(view, "Tài khoản hoặc mật khẩu bị sai", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
                else {
                    Snackbar.make(view, "Tài khoản hoặc mật khẩu bị sai", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            }
        }
        if(!TextUtils.isEmpty(accountInfo.getRole())) {
            if(accountInfo.getRole().equals("Sinh viên")) {
                Task<QuerySnapshot> libraryCardInfo = libraryCardCollectionRef.whereEqualTo("Id", accountInfo.getAccount()).get();
                while (true) {
                    if (libraryCardInfo.isSuccessful()) {
                        for (DocumentSnapshot idCart : libraryCardInfo.getResult())
                            libraryCard = new LibraryCard(accountInfo.getAccount(),
                                    idCart.getString("Name"),
                                    idCart.getString("ExpirationDate"),
                                    idCart.getBoolean("Status"),
                                    idCart.getBoolean("Borrow"));

                        Snackbar.make(view, "Đăng nhập thành công", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        MainActivity.instance.currentFragment = new AccountFragment();
                        MainActivity.instance.ReplaceFragment(-1);
                        MainActivity.instance.isLogin = true;
                        break;
                    }
                }
            }
            else if(accountInfo.getRole().equals("Quản lý") || accountInfo.getRole().equals("Thủ kho") || accountInfo.getRole().equals("Thủ thư"))
            {
                //MainActivity.instance.currentFragment = new ManageListFragment();
                MainActivity.instance.ReplaceFragment(-1);
                MainActivity.instance.isLogin = true;
            }
        }
    }

    // Tải Tài khoản - Manager
    public static void LoadAccounts(String roleName)
    {
        accNames = new ArrayList<>();
        accounts = new ArrayList<>();
        Task<QuerySnapshot> accountNames = null;
        if(BookstoreProjectDatabase.accountInfo.getRole().equals("Quản lý"))
            accountNames = accountCollectionRef.whereNotEqualTo("Role", roleName).get();
        else if(BookstoreProjectDatabase.accountInfo.getRole().equals("Thủ thư"))
            accountNames = accountCollectionRef.whereEqualTo("Role", roleName).get();

        while(true)
        {
            if(accountNames.isSuccessful())
            {
                for (DocumentSnapshot accountName : accountNames.getResult()) {
                    if (!accountName.getString("Role").equals("Quản lý")) {
                        accNames.add(accountName.getString("Account"));
                        accounts.add(new Account(accountName.getString("Account"), accountName.getString("Password"), accountName.getString("Role")));
                        System.out.println(accountName.getString("Account"));
                    }
                }
                break;
            }
        }
    }

    // Tải Tài khoản - Manager
    public static void LoadAccounts()
    {
        accNames = new ArrayList<>();
        accounts = new ArrayList<>();
        Task<QuerySnapshot> accountNames = accountCollectionRef.get();
        while(true)
        {
            if(accountNames.isSuccessful())
            {
                for (DocumentSnapshot accountName : accountNames.getResult())
                {
                    accNames.add(accountName.getString("Account"));
                    accounts.add(new Account(accountName.getString("Account"), accountName.getString("Password"), accountName.getString("Role")));
                    System.out.println(accountName.getString("Account"));
                }
                break;
            }
        }
    }

    public static void LoadAccountWithId(String roleName, String keyWord)
    {
        accounts = new ArrayList<>();
        Task<QuerySnapshot> accountNames = accountCollectionRef.whereEqualTo("Role", roleName).get();
        while(true)
        {
            if(accountNames.isSuccessful())
            {
                for (DocumentSnapshot accountName : accountNames.getResult())
                {
                    if(accountName.getString("Account").contains(keyWord)) {
                        accounts.add(new Account(accountName.getString("Account"), accountName.getString("Password"), accountName.getString("Role")));
                        System.out.println(accountName.getString("Account"));
                    }
                }
                break;
            }
        }
    }
    // tải thẻ sinh viên - Manager
    public static void LoadLibraryCards()
    {
        libraryCards = new ArrayList<>();
        Task<QuerySnapshot> libraryCardIds = libraryCardCollectionRef.get();
        while(true)
        {
            if(libraryCardIds.isSuccessful())
            {
                for (DocumentSnapshot libraryCardId : libraryCardIds.getResult())
                {
                    libraryCards.add(new LibraryCard(libraryCardId.getString("Id"),
                            libraryCardId.getString("Name"),
                            libraryCardId.getString("ExpirationDate"),
                            libraryCardId.getBoolean("Status"),
                            libraryCardId.getBoolean("Borrow")));
                }
                break;
            }
        }
    }

    // tải thẻ sinh viên - Manager
    public static void LoadLibraryCardsWithId(@NonNull String id)
    {
        libraryCards = new ArrayList<>();
        Task<QuerySnapshot> libraryCardIds = libraryCardCollectionRef.get();
        while(true)
        {
            if(libraryCardIds.isSuccessful())
            {
                for (DocumentSnapshot libraryCardId : libraryCardIds.getResult())
                {
                    if(libraryCardId.getString("Id").contains(id))
                        libraryCards.add(new LibraryCard(libraryCardId.getString("Id"),
                                libraryCardId.getString("Name"),
                                libraryCardId.getString("ExpirationDate"),
                                libraryCardId.getBoolean("Status"),
                                libraryCardId.getBoolean("Borrow")));
                }
                break;
            }
        }
    }

    // tải thẻ sinh viên - Manager
    public static void LoadLibraryCardsWithName(@NonNull String name)
    {
        libraryCards = new ArrayList<>();
        Task<QuerySnapshot> libraryCardIds = libraryCardCollectionRef.get();
        while(true)
        {
            if(libraryCardIds.isSuccessful())
            {
                for (DocumentSnapshot libraryCardId : libraryCardIds.getResult())
                {
                    if(libraryCardId.getString("Name").contains(name))
                        libraryCards.add(new LibraryCard(libraryCardId.getString("Id"),
                                libraryCardId.getString("Name"),
                                libraryCardId.getString("ExpirationDate"),
                                libraryCardId.getBoolean("Status"),
                                libraryCardId.getBoolean("Borrow")));
                }
                break;
            }
        }
    }

    public static void SortAcccount(String roleName, boolean isAsc)
    {
        accounts = new ArrayList<>();

        Task<QuerySnapshot> accountNames = accountCollectionRef.whereEqualTo("Role", roleName).orderBy("Account", isAsc ? Query.Direction.ASCENDING : Query.Direction.DESCENDING).get();
        while(true)
        {
            if(accountNames.isSuccessful())
            {
                for (DocumentSnapshot accountName : accountNames.getResult())
                {
                    accounts.add(new Account(accountName.getString("Account"), accountName.getString("Password"), accountName.getString("Role")));
                    System.out.println(accountName.getString("Account"));
                }
                break;
            }
        }
    }

    public static void SortBookWithName(boolean isAsc, String genre)
    {
        System.out.println("Thể loại: " + genre);
        books = new ArrayList<>();
        String content = "";
        Task<QuerySnapshot> bookIds = bookCollectionRef.orderBy("Name", isAsc ? Query.Direction.ASCENDING : Query.Direction.DESCENDING).get();
        while(true) {
            if (bookIds.isSuccessful()) {
                for (DocumentSnapshot id : bookIds.getResult()) {
                    content = "";
                    for(String arCon : (List<String>)id.get("Content"))
                    {
                        content += arCon + "\n";
                    }
                    books.add(new Book(id.getId(),
                            id.getString("Name"),
                            id.getString("Author"),
                            id.getString("Genre"),
                            content,
                            id.getString("YearPublished"),
                            id.getString("Publisher"),
                            id.getString("URL")));
                    System.out.println("Book name " + id.getId() + " : " + id.getString("Name"));
                }
                break;
            }
        }
    }

    public static void SortBookWithYearPublished(boolean isAsc, String genre)
    {
        System.out.println("Thể loại: " + genre);
        books = new ArrayList<>();
        String content = "";
        Task<QuerySnapshot> bookIds = bookCollectionRef.orderBy("YearPublished", isAsc ? Query.Direction.ASCENDING : Query.Direction.DESCENDING).get();
        while(true) {
            if (bookIds.isSuccessful()) {
                for (DocumentSnapshot id : bookIds.getResult()) {
                    content = "";
                    for(String arCon : (List<String>)id.get("Content"))
                    {
                        content += arCon + "\n";
                    }
                    books.add(new Book(id.getId(),
                            id.getString("Name"),
                            id.getString("Author"),
                            id.getString("Genre"),
                            content,
                            id.getString("YearPublished"),
                            id.getString("Publisher"),
                            id.getString("URL")));
                    System.out.println("Book name " + id.getId() + " : " + id.getString("Name"));
                }
                break;
            }
        }
    }

    public static void SortLibraryCard(boolean isAsc)
    {
        libraryCards = new ArrayList<>();
        Task<QuerySnapshot> libraryCardIds = libraryCardCollectionRef.orderBy("Id", isAsc ? Query.Direction.ASCENDING : Query.Direction.DESCENDING).get();
        while(true)
        {
            if(libraryCardIds.isSuccessful())
            {
                for (DocumentSnapshot libraryCardId : libraryCardIds.getResult())
                {
                    libraryCards.add(new LibraryCard(libraryCardId.getString("Id"),
                            libraryCardId.getString("Name"),
                            libraryCardId.getString("ExpirationDate"),
                            libraryCardId.getBoolean("Status"),
                            libraryCardId.getBoolean("Borrow")));
                }
                break;
            }
        }
    }

    public static void LoadLoan()
    {
        Boolean isDone = false;
        loans = new ArrayList<>();
        Task<QuerySnapshot> loanIds = loanCollectionRef.get();
        while(true)
        {
            if(loanIds.isSuccessful())
            {
                int i = 0;
                for(DocumentSnapshot loanId : loanIds.getResult()) {
                    for (Book book : books) {
                        Task<QuerySnapshot> bookCopyIds = loanCollectionRef.document(loanId.getId()).collection(book.getId()).get();
                        while(true)
                        {
                            if(bookCopyIds.isSuccessful())
                            {
                                for(DocumentSnapshot bookCopyId : bookCopyIds.getResult())
                                {
                                    loans.add(new Loan(book.getId(),
                                            loanId.getId(),
                                            bookCopyId.getString("BookCopyId"),
                                            bookCopyId.getString("BorrowDate"),
                                            bookCopyId.getString("DateDue")));
                                    System.out.println(book.getId() + ", " + loanId.getId());
                                }
                                break;
                            }
                        }
                    }
                    i++;
                }
                if(i == loanIds.getResult().size())
                    break;
            }
        }
    }

    public static void SortLoan(boolean isAsc)
    {
        loans = new ArrayList<>();
        Task<QuerySnapshot> loanIds = loanCollectionRef.get();
        while(true)
        {
            if(loanIds.isSuccessful())
            {
                for(DocumentSnapshot loanId : loanIds.getResult()) {
                    for (Book book : books) {
                        Task<QuerySnapshot> bookCopyIds = loanCollectionRef.document(loanId.getId()).collection(book.getId()).orderBy("BorrowDate", isAsc ? Query.Direction.ASCENDING : Query.Direction.DESCENDING).get();
                        while(true)
                        {
                            if(bookCopyIds.isSuccessful())
                            {
                                for(DocumentSnapshot bookCopyId : bookCopyIds.getResult())
                                {
                                    loans.add(new Loan(book.getId(),
                                            loanId.getId(),
                                            bookCopyId.getString("BookCopyId"),
                                            bookCopyId.getString("BorrowDate"),
                                            bookCopyId.getString("DateDue")));
                                }
                                break;
                            }
                            else
                            {
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    public static void SortCopies(boolean isAsc)
    {
        copies = new ArrayList<>();

        Task<QuerySnapshot> bookIds = copyCollectionRef.orderBy("Id", isAsc ? Query.Direction.ASCENDING : Query.Direction.DESCENDING).get();
        while(true) {
            if (bookIds.isSuccessful()) {
                for (DocumentSnapshot id : bookIds.getResult()) {
                    Task<QuerySnapshot> copyIds = copyCollectionRef.document(id.getId()).collection("BookCopy").get();
                    while(true) {
                        if (copyIds.isSuccessful()) {
                            for (DocumentSnapshot copy : copyIds.getResult()) {
                                copies.add(new Copy(copy.getId(),
                                        id.getId(),
                                        copy.getString("Status"),
                                        copy.getString("Notes")));
                            }
                            break;
                        }
                    }
                }
                break;
            }
        }
    }


    // Thêm sách - Manager
    public static void AddBook(@NonNull Book book)
    {
        ArrayList<String> contentParts = new ArrayList<>();
        String[] parts =  book.getContent().split("\n");

        for(String part : parts)
        {
            contentParts.add(part);
        }


        HashMap<String, Object> newBook = new HashMap<>();
        newBook.put("Author", book.getAuthor());
        newBook.put("Content", contentParts);
        newBook.put("Genre", book.getGenre());
        newBook.put("Id", book.getId());
        newBook.put("Name", book.getTitle());
        newBook.put("Publisher", book.getPublisher());
        newBook.put("URL", book.getUrlImage());
        newBook.put("YearPublished", book.getYearPublished());

        bookCollectionRef.document(book.getId()).set(newBook);
        System.out.println("Thêm sách thành công: " + newBook);

    }

    // cập nhập sách - Manager
    public static void UpdateBook(@NonNull Book book)
    {
        ArrayList<String> contentParts = new ArrayList<>();
        String[] parts =  book.getContent().split("\n");

        for(String part : parts)
        {
            contentParts.add(part);
        }


        HashMap<String, Object> newBook = new HashMap<>();
        newBook.put("Author", book.getAuthor());
        newBook.put("Content", contentParts);
        newBook.put("Genre", book.getGenre());
        newBook.put("Name", book.getTitle());
        newBook.put("Publisher", book.getPublisher());
        newBook.put("URL", book.getUrlImage());
        newBook.put("YearPublished", book.getYearPublished());

        bookCollectionRef.document(book.getId()).update(newBook);
        System.out.println("Chỉnh sửa sách thành công: " + newBook);
    }

    // xóa sách - Manager
    public static void DeleteBook(@NonNull String id)
    {
        bookCollectionRef.document(id).delete();
    }

    // Thêm bản sao của sách - Manager
    public static void AddBookCopy(@NonNull Copy copy)
    {
        HashMap<String, Object> newBookCopy = new HashMap<>();

        newBookCopy.put("Id",copy.getBookId());
        copyCollectionRef.document(copy.getBookId()).set(newBookCopy);
        newBookCopy = new HashMap<>();

        newBookCopy.put("Notes",copy.getNotes());
        newBookCopy.put("Status",copy.getStatus());

        copyCollectionRef.document(copy.getBookId()).collection("BookCopy").document(copy.getId()).set(newBookCopy);
    }
    // cập nhật bản sao của sách - Manager
    public static void UpdateBookCopy(@NonNull Copy copy)
    {
        HashMap<String, Object> updateBookCopy = new HashMap<>();

        updateBookCopy.put("Notes",copy.getNotes());
        updateBookCopy.put("Status",copy.getStatus());

        copyCollectionRef.document(copy.getBookId()).collection("BookCopy").document(copy.getId()).update(updateBookCopy);
    }
    // xóa bản sao của sách - Manager
    public static void DeleteBookCopy(@NonNull String BookId,@NonNull String id)
    {

        copyCollectionRef.document(BookId).collection("BookCopy").document(id).delete();
    }

    public static void DeleteBookCopy(@NonNull String BookId)
    {

        copyCollectionRef.document(BookId).delete();
    }

    // Thêm thẻ thư viện - Manager
    public static boolean AddLibraryCard(@NonNull LibraryCard libraryCard)
    {
        if (libraryCardCollectionRef.document(libraryCard.getId()) != null)
        {
            return false;
        }
        HashMap<String, Object> libraryCardData = new HashMap<>();

        libraryCardData.put("Borrow", libraryCard.getBorrowStatus());
        libraryCardData.put("ExpirationDate", libraryCard.getExpirationDate());
        libraryCardData.put("Id", libraryCard.getId());
        libraryCardData.put("Name", libraryCard.getName());
        libraryCardData.put("Status", libraryCard.getUseStatus());

        libraryCardCollectionRef.document(libraryCard.getId()).set(libraryCardData);
        return true;
    }
    // cập nhật thẻ thư viện
    public static void UpdateLibraryCard(@NonNull LibraryCard libraryCard, boolean borrowStatus, boolean useStatus)
    {
        libraryCardCollectionRef.document(libraryCard.getId()).update("Borrow", borrowStatus);
        libraryCardCollectionRef.document(libraryCard.getId()).update("Status", useStatus);
    }

    // cập nhật thẻ thư viện
    public static void UpdateLibraryCard(@NonNull LibraryCard libraryCard, boolean useStatus, String ExpirationDate)
    {
        libraryCardCollectionRef.document(libraryCard.getId()).update("ExpirationDate", ExpirationDate);
        libraryCardCollectionRef.document(libraryCard.getId()).update("Status", useStatus);
    }

    // xóa thẻ thư viện - Manager
    public static void DeleteLibraryCard(@NonNull String id)
    {
        libraryCardCollectionRef.document(id).delete();
    }
    // Thêm Lần mượn sách- Manager
    public static void AddLoan(@NonNull Loan loan)
    {
        HashMap<String, Object> multiData = new HashMap<>();
        multiData.put("Id", loan.getCardId());
        loanCollectionRef.document(loan.getCardId()).set(multiData);

        multiData = new HashMap<>();
        multiData.put("BorrowDate", loan.getDateLoaned());
        multiData.put("DateDue", loan.getDateDue());
        multiData.put("BookCopyId", loan.getCopyId());
        loanCollectionRef.document(loan.getCardId()).collection(loan.getBookId()).document().set(multiData);
    }
    // Thêm tài khoản - Manager
    public static boolean AddAccount(@NonNull Account account)
    {
        if (accountCollectionRef.document(account.getAccount()) != null)
        {
            return false;
        }
        //  if(accountCollectionRef.whereEqualTo("Account", account.getAccount()))
        HashMap<String, Object> newAccount = new HashMap<>();
        newAccount.put("Account", account.getAccount());
        newAccount.put("Password", account.getPassword());
        newAccount.put("Role", account.getRole());
        newAccount.put("isLogin", false);

        accountCollectionRef.document(account.getAccount()).set(newAccount);
        return true;
    }
    // Cập nhật tài khoản - Manager
    public static boolean UpdateAccount(@NonNull String account, @NonNull String password) {
        Task<QuerySnapshot> accId = accountCollectionRef.whereEqualTo("Account", account).get();
        while (true)
        {
            if(accId.isSuccessful())
            {
                if (accId.getResult().size() != 0) {
                    accountCollectionRef.document(account).update("Password", password);
                    return true;
                }
                else
                    return false;
            }
        }
    }

    // Cập nhật tài khoản - Manager
    public static boolean UpdateAccount(@NonNull String account, @NonNull boolean isLogin) {
        Task<QuerySnapshot> accId = accountCollectionRef.whereEqualTo("Account", account).get();
        while (true)
        {
            if(accId.isSuccessful())
            {
                if (accId.getResult().size() != 0) {
                    accountCollectionRef.document(account).update("isLogin", isLogin);
                    return true;
                }
                else
                    return false;
            }
        }
    }

    // Xóa tài khoản - Manager
    public static void DeleteAccount(@NonNull String nameAccount)
    {
        accountCollectionRef.document(nameAccount).delete();
    }

}

