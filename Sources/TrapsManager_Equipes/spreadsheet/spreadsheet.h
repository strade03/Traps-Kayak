#ifndef SPREADSHEET_H
#define SPREADSHEET_H

#include <QObject>
#include <QSet>
#include <QAbstractListModel>

class SpreadSheet : public QAbstractListModel
{

    Q_OBJECT

    Q_PROPERTY(int tableColumnCount READ tableColumnCount NOTIFY tableColumnCountChanged)
    Q_PROPERTY(int leftColumnCount READ leftColumnCount NOTIFY tableColumnCountChanged)

    Q_PROPERTY(QList<double> columnWidthList READ columnWidthList NOTIFY columnListChanged)
    Q_PROPERTY(QStringList columnNameList READ columnNameList NOTIFY columnListChanged)
    Q_PROPERTY(QList<bool> resizableColumnList READ resizableColumnList NOTIFY columnListChanged)
    Q_PROPERTY(QList<bool> sortEnabledColumnList READ sortEnabledColumnList NOTIFY columnListChanged)


public:

    SpreadSheet(int columnCount, const QStringList &columnNameList, const QList<double>& columnWidthList, int leftColumnCount=0);
    virtual ~SpreadSheet();

    void setColumnList(int columnCount, const QStringList &columnNameList, const QList<double>& columnWidthList, int leftColumnCount=0);
    void setColumnName(int index, const QString& name);
    void setColumnListVisible(QList<int> columnList, bool visible);
    void setResizableForColumn(int index, bool resizable);
    void setSortEnabledForColumn(int index, bool sortEnabled);
    void setBgColorForColumn(int index, const QString& color);
    void setTextColorForColumn(int index, const QString& color);
    void setFontFamilyForColumn(int index, const QString& fontFamily);
    void setCheckableForColumn(int index);
    void setReadOnlyForColumn(int index);
    void setActionEnabledForColumn(int index);
    void setComboModelForColumn(int index, const QStringList& comboModel);
    void setTextAlignmentForColumn(int index, int alignment);

    QStringList columnNameList() const { return _columnNameList; }
    QList<double> columnWidthList() const;
    QList<bool> resizableColumnList() const;
    QList<bool> sortEnabledColumnList() const;


signals:

    void columnListChanged();
    void tableColumnCountChanged();
    void popupContextMenu(int rowIndex, int columnIndex, QStringList menuList);

public slots:

    virtual void requestCheckedChange(int rowIndex, int columnIndex, bool checked);
    virtual void requestComboIndexChange(int rowIndex, int columnIndex, int index);
    virtual void requestTextChange(int rowIndex, int columnIndex, const QString& text);
    virtual void requestAction(int rowIndex, int columnIndex);
    virtual void sortByColumn(int index, bool asc);
    virtual void contextMenuAction(int rowIndex, int columnIndex, int menuIndex);
    void requestContextMenu(int rowIndex, int columnIndex);
    void selectRow(int index);
    void selectRowRange(int startIndex, int endIndex);
    void clearRowSelection();
    void setColumnWidth(int index, double width);


protected:

    virtual QString text(int rowIndex, int columnIndex) const;
    virtual QString bgColor(int rowIndex, int columnIndex) const;
    virtual QString textColor(int rowIndex, int columnIndex) const;
    virtual int comboIndex(int rowIndex, int columnIndex) const;
    virtual int textAlignment(int rowIndex, int columnIndex) const;
    virtual QString fontFamily(int rowIndex, int columnIndex) const;
    virtual QStringList comboModel(int rowIndex, int columnIndex) const;
    virtual QStringList contextMenuList(int rowIndex, int columnIndex) const;
    virtual bool readOnly(int rowIndex, int columnIndex) const;
    virtual bool action(int rowIndex, int columnIndex) const;
    virtual bool checkable(int rowIndex, int columnIndex) const;
    virtual bool checked(int rowIndex, int columnIndex) const;
    virtual int rowCount() const = 0;

    int rowCount(const QModelIndex &parent) const;
    QVariant data(const QModelIndex &index, int role) const;

    QHash<int, QByteArray> roleNames() const;

    void rowUpdated(int rowIndex);

    int leftColumnCount() const { return _leftColumnCount; }
    int tableColumnCount() const { return _columnCount; }

    QSet<int> selectedRowSet() const { return _selectedRowSet; }


private:

    enum {

        readOnlyList = Qt::UserRole,
        textList, // text to be displayed
        checkableList,
        checkedList,  // checkbox checked ?
        comboIndexList,
        comboModelList,
        bgColorList,
        textColorList,
        textAlignmentList,
        fontFamilyList,
        actionList,  // booleans. if true, then call action method on double click instead of opening editor.

    };


    class RowModel  {

    public:

        QHash<int, QString> textMap;
        QHash<int, bool> checkedMap;
        QHash<int, int> comboIndexMap;

    };

    QStringList _columnNameList;
    QSet<int> _checkableColumnSet;
    QSet<int> _readOnlyColumnSet;
    QSet<int> _actionColumnSet;
    QHash<int, QStringList> _columnComboModel;
    QHash<int, int> _columnTextAlignment;
    QHash<int, QString> _columnBgColor;
    QHash<int, QString> _columnTextColor;
    QHash<int, QString> _columnFontFamily;
    QSet<int> _hiddenColumnSet;
    QHash<int, bool> _resizableColumn;
    QHash<int, bool> _sortEnabledColumn;
    QSet<int> _selectedRowSet;

    int _columnCount;
    int _leftColumnCount;

    QList<double> _columnWidthList;

    QHash<int, RowModel*> _internalModel;
    RowModel* internalRow(int rowIndex);

};

#endif // SPREADSHEET_H
