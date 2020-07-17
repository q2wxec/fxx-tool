
package com.fxx.common.tools.test.print;

import com.fxx.common.tools.bean.print.*;
import com.fxx.common.tools.utils.CollUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author wangxiao1
 * @date 2019/9/2514:02
 */
@LogIdForListCompare(idField = "id")
public class TestBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @LogLable(name = "ID", alwaysLogWhenEdit = true)
    private Long id;

    @LogLable(name = "名称")
    private String name;

    @LogLable(name = "责任单位")
    private String departmentID;

    @LogLable(name = "所在城市")
    //@LogTypeFormat(logTypes = {"1,武汉","2,上海"})
    @LogFormat(formatInter = ProvinceEnum.class)
    private Integer province;

    private String provinceName;

    @LogLable(name = "所在区县")
    private Integer city;

    private String cityName;

    @LogLable(name = "注册地址")
    private String regAddress;

    @LogLable(name = "经营范围")
    private String manageRange;

    @LogLable(name = "注册资本(万元)")
    private BigDecimal regFund;

    @LogLable(name = "设立日期")
    @LogDateFormat(format = "yyyy-MM-dd")
    private Date setUpDate;

    @LogLable(name = "经营期限开始")
    @LogDateFormat(format = "yyyy-MM-dd")
    private Date manageBeginDate;

    @LogLable(name = "经营期限结束")
    @LogDateFormat(format = "yyyy-MM-dd")
    private Date manageEndDate;

    public TestBean(Long id, String name, String departmentID, BigDecimal regFund, Date setUpDate) {
        this.id = id;
        this.name = name;
        this.departmentID = departmentID;
        this.regFund = regFund;
        this.setUpDate = setUpDate;
    }

    public TestBean() {

    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartmentID() {
        return departmentID;
    }

    public void setDepartmentID(String departmentID) {
        this.departmentID = departmentID;
    }

    public Integer getProvince() {
        return province;
    }

    public void setProvince(Integer province) {
        this.province = province;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public Integer getCity() {
        return city;
    }

    public void setCity(Integer city) {
        this.city = city;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getRegAddress() {
        return regAddress;
    }

    public void setRegAddress(String regAddress) {
        this.regAddress = regAddress;
    }

    public String getManageRange() {
        return manageRange;
    }

    public void setManageRange(String manageRange) {
        this.manageRange = manageRange;
    }

    public BigDecimal getRegFund() {
        return regFund;
    }

    public void setRegFund(BigDecimal regFund) {
        this.regFund = regFund;
    }

    public Date getSetUpDate() {
        return setUpDate;
    }

    public void setSetUpDate(Date setUpDate) {
        this.setUpDate = setUpDate;
    }

    public Date getManageBeginDate() {
        return manageBeginDate;
    }

    public void setManageBeginDate(Date manageBeginDate) {
        this.manageBeginDate = manageBeginDate;
    }

    public Date getManageEndDate() {
        return manageEndDate;
    }

    public void setManageEndDate(Date manageEndDate) {
        this.manageEndDate = manageEndDate;
    }

    public static void main(String[] args) throws Exception {
        TestBean before = new TestBean();
        TestBean after = new TestBean();
        before.setId(1L);
        after.setId(1L);
        before.setName("wuhan");
        before.setProvince(1);
        before.setSetUpDate(new Date(System.currentTimeMillis()));
        after.setSetUpDate(new Date(System.currentTimeMillis() + 1000000000));
        after.setName("shanghai");
        after.setProvince(2);
        List<Object> beforeList = CollUtils.newArrayList();
        List<Object> afterList = CollUtils.newArrayList();
        TestBean b1 = new TestBean(3L, "lalala", "123", new BigDecimal("12.3456"), new Date());
        TestBean b11 = new TestBean(3L, "lala", "12d3", new BigDecimal("123.3456"), new Date());
        TestBean b2 = new TestBean(4L, "lalala", "123", new BigDecimal("12.3456"), new Date());
        TestBean b22 = new TestBean(4L, "lalala", "123", new BigDecimal("12.3456"), new Date());
        TestBean b3 = new TestBean(5L, "lalala", "123", new BigDecimal("12.3456"), new Date());
        TestBean b33 = new TestBean(5L, "lalala", "13", new BigDecimal("12.3456"), new Date());
        TestBean b4 = new TestBean(6L, "lalala", "123", new BigDecimal("12.3456"), new Date());
        TestBean b55 = new TestBean(7L, "lalala", "123", new BigDecimal("12.3456"), new Date());
        beforeList.add(b1);
        beforeList.add(b2);
        beforeList.add(b3);
        beforeList.add(b4);

        afterList.add(b11);
        afterList.add(b22);
        afterList.add(b33);
        afterList.add(b55);

        UpdateLogResult updateLogResult = LogUtil.updateLogDoNotIgnorNull(before, after, beforeList, afterList, "商品列表");

        System.out.println("before:");
        System.out.println(updateLogResult.getBefore());

        System.out.println("after:");
        System.out.println(updateLogResult.getAfter());

    }
}
