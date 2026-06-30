package com.kskj.controller;



import com.kskj.pojo.Menu;
import com.kskj.pojo.User;
import com.kskj.service.MenuService;
import com.kskj.service.UserService;
import com.kskj.until.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  前端控制器：用户管理和管理员管理模块
 * </p>
 *
 * @author rabbiter
 * @since 2023-01-02
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private MenuService menuService;
//
//    /*
//     * 查询全部用户
//     * @author rabbiter
//     * @date 2023/1/2 19:26
//     */
//    @GetMapping("/list")
//    public List<User> list(){
//        return userService.list();
//    }
//
//    /*
//     * 根据账号查找用户
//     * @author rabbiter
//     * @date 2023/1/4 14:53
//     */
//    @GetMapping("/findByNo")
//    public R findByNo(@RequestParam String no){
//        List list = userService.lambdaQuery()
//                .eq(User::getNo,no)
//                .list();
//        return list.size()>0?R.ok(list):R.fail();
//    }
//
//    /*
//     * 新增用户
//     * @author rabbiter
//     * @date 2023/1/2 19:11
//     */
//    @PostMapping("/save")
//    public R save(@RequestBody User user){
//        return userService.save(user)?R.ok():R.fail();
//    }
//
//    /*
//     * 更新用户
//     * @author rabbiter
//     * @date 2023/1/2 19:11
//     */
//    @PostMapping("/update")
//    public R update(@RequestBody User user){
//        return userService.updateById(user)?R.ok():R.fail();
//    }

    /*
     * 用户登录：登录的时候一并将菜单信息也查询出来
     * @author rabbiter
     * @date 2023/1/3 14:08
     */
//    @PostMapping("/login")
//
//    public R login(@RequestBody User user){
//        //匹配账号和密码
//
//        List list = userService.lambdaQuery()
//                .eq(User::getNo,user.getNo())
//                .eq(User::getPassword,user.getPassword())
//                .list();
//
//        if(list.size()>0){
//            User user1 = (User)list.get(0);
//            List<Menu> menuList = menuService.lambdaQuery()
//                    .like(Menu::getMenuright,user1.getRoleId())
//                    .list();
//            HashMap res = new HashMap();
//            res.put("user",user1);
//            res.put("menu",menuList);
//            return R.ok(res);
//        }
//        return R.fail();
//    }

//    /*
//     * 修改用户
//     * @author rabbiter
//     * @date 2023/1/4 15:02
//     */
//    @PostMapping("/mod")
//    public boolean mod(@RequestBody User user){
//        return userService.updateById(user);
//    }
//
//    /*
//     * 新增或修改：存在用户则修改，否则新增用户
//     * @author rabbiter
//     * @date 2023/1/2 19:12
//     */
//    @PostMapping("/saveOrUpdate")
//    public R saveOrUpdate(@RequestBody User user){
//        return userService.saveOrUpdate(user)?R.ok():R.fail();
//    }
//
//    /*
//     * 删除用户
//     * @author rabbiter
//     * @date 2023/1/2 19:15
//     */
//    @GetMapping("/del")
//    public R delete(Integer id){
//        return userService.removeById(id)?R.ok():R.fail();
//    }
//
//    /*
//     * 模糊查询
//     * @author rabbiter
//     * @date 2023/1/2 19:36
//     */
//    @PostMapping("/listP")
//    public R query(@RequestBody User user){
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//        if(StringUtils.isNotBlank(user.getName())){
//            wrapper.like(User::getName,user.getName());
//        }
//        return R.ok(userService.list(wrapper));
//    }

    /*
     * 分页查询
     * @author rabbiter
     * @date 2023/1/2 19:48
     */
//    @PostMapping("/listPage")
//    public Result page(@RequestBody QueryPageParam query){
//        HashMap param = query.getParam();
//        String name = (String)param.get("name");
//
//        Page<User> page = new Page();
//        page.setCurrent(query.getPageNum());
//        page.setSize(query.getPageSize());
//
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//        wrapper.like(User::getName,name);
//
//        IPage result = userService.page(page,wrapper);
//        return Result.success(result.getRecords(),result.getTotal());
//    }

//    @PostMapping("/listPage")
//    public List<User> listPage(@RequestBody QueryPageParam query){
//        HashMap param = query.getParam();
//        String name = (String)param.get("name");
//        System.out.println("name=>"+(String)param.get("name"));
//
//        Page<User> page = new Page();
//        page.setCurrent(query.getPageNum());
//        page.setSize(query.getPageSize());
//
//        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
//        lambdaQueryWrapper.like(User::getName,name);
//
//
//        IPage result = userService.page(page,lambdaQueryWrapper);
//
//        System.out.println("total=>"+result.getTotal());
//
//        return result.getRecords();
//    }
//
//    /*
//     * 查询功能：根据前端表单输入的信息或者下拉框选择查询用户，并以分页的形式返回前端
//     * @author rabbiter
//     * @date 2023/1/4 20:28
//     */
//    @PostMapping("/listPageC1")
//    public R listPageC1(@RequestBody QueryPageParam query){
//        HashMap param = query.getParam();
//        String name = (String)param.get("name");
//        String sex = (String)param.get("sex");
//        String roleId = (String)param.get("roleId");
//
//        Page<User> page = new Page();
//        page.setCurrent(query.getPageNum());
//        page.setSize(query.getPageSize());
//
//        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
//        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
//            lambdaQueryWrapper.like(User::getName,name);
//        }
//        if(StringUtils.isNotBlank(sex)){
//            lambdaQueryWrapper.eq(User::getSex,sex);
//        }
//        if(StringUtils.isNotBlank(roleId)){
//            lambdaQueryWrapper.eq(User::getRoleId,roleId);
//        }
//
//        IPage result = userService.pageCC(page,lambdaQueryWrapper);
//
//        System.out.println("total=>"+result.getTotal());
//
//        return R.ok(result.getRecords(),result.getTotal());
//    }

}
