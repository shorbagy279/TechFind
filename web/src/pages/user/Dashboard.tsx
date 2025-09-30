import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  Paper,
  Avatar,
  Divider
} from '@mui/material';
import {
  Search,
  History,
  Favorite,
  Person,
  Payment
} from '@mui/icons-material';
import { auth } from '../../store/auth';
import client from '../../api/client';

export default function Dashboard() {
  const { t, i18n } = useTranslation();
  const userAuth = auth.get();

  const handlePaymentTest = async () => {
    try {
      const { data } = await client.post('/payments/create-session', {
        amount: 100,
        currency: 'EGP',
        serviceName: 'Test Service',
        description: 'Test payment',
        customerEmail: userAuth.fullName,
        paymentProvider: 'stripe'
      });
      
      if (data.sessionUrl) {
        window.location.href = data.sessionUrl;
      }
    } catch (error) {
      console.error('Payment error:', error);
    }
  };

  const quickActions = [
    {
      title: i18n.language === 'ar' ? 'بحث عن فني' : 'Find Technician',
      description: i18n.language === 'ar' 
        ? 'ابحث عن الفنيين في منطقتك'
        : 'Search for technicians in your area',
      icon: <Search sx={{ fontSize: 40 }} />,
      path: '/search',
      color: 'primary.main'
    },
    {
      title: i18n.language === 'ar' ? 'المفضلة' : 'Favorites',
      description: i18n.language === 'ar'
        ? 'الفنيين المفضلين لديك'
        : 'Your favorite technicians',
      icon: <Favorite sx={{ fontSize: 40 }} />,
      path: '/favorites',
      color: 'error.main'
    },
    {
      title: i18n.language === 'ar' ? 'السجل' : 'History',
      description: i18n.language === 'ar'
        ? 'سجل الحجوزات السابقة'
        : 'Your booking history',
      icon: <History sx={{ fontSize: 40 }} />,
      path: '/history',
      color: 'success.main'
    }
  ];

  return (
    <Box>
      {/* Welcome Section */}
      <Paper sx={{ p: 4, mb: 4, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
          <Avatar 
            sx={{ 
              width: 80, 
              height: 80, 
              bgcolor: 'white', 
              color: 'primary.main',
              fontSize: 32,
              fontWeight: 'bold'
            }}
          >
            {userAuth.fullName?.charAt(0) || <Person />}
          </Avatar>
          <Box>
            <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
              {i18n.language === 'ar' ? 'مرحباً' : 'Welcome'}, {userAuth.fullName}!
            </Typography>
            <Typography variant="body1">
              {i18n.language === 'ar'
                ? 'نحن هنا لمساعدتك في العثور على أفضل الفنيين'
                : "We're here to help you find the best technicians"}
            </Typography>
          </Box>
        </Box>
      </Paper>

      {/* Quick Actions */}
      <Typography variant="h5" gutterBottom sx={{ mb: 3, fontWeight: 'bold' }}>
        {i18n.language === 'ar' ? 'الإجراءات السريعة' : 'Quick Actions'}
      </Typography>
      
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {quickActions.map((action, index) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={index}>
            <Card
              sx={{
                height: '100%',
                cursor: 'pointer',
                transition: 'all 0.3s',
                '&:hover': {
                  transform: 'translateY(-8px)',
                  boxShadow: 6
                }
              }}
              component={Link}
              to={action.path}
              style={{ textDecoration: 'none' }}
            >
              <CardContent sx={{ textAlign: 'center', p: 3 }}>
                <Avatar
                  sx={{
                    width: 70,
                    height: 70,
                    margin: '0 auto 16px',
                    bgcolor: action.color
                  }}
                >
                  {action.icon}
                </Avatar>
                <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                  {action.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {action.description}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Divider sx={{ my: 4 }} />

      {/* Recent Activity */}
      <Typography variant="h5" gutterBottom sx={{ mb: 3, fontWeight: 'bold' }}>
        {i18n.language === 'ar' ? 'النشاط الأخير' : 'Recent Activity'}
      </Typography>

      <Card>
        <CardContent>
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <History sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
            <Typography variant="body1" color="text.secondary" gutterBottom>
              {i18n.language === 'ar'
                ? 'لا يوجد نشاط حتى الآن'
                : 'No activity yet'}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              {i18n.language === 'ar'
                ? 'ابدأ بالبحث عن فني للبدء'
                : 'Start by searching for a technician'}
            </Typography>
            <Button
              variant="contained"
              startIcon={<Search />}
              component={Link}
              to="/search"
            >
              {i18n.language === 'ar' ? 'ابحث الآن' : 'Search Now'}
            </Button>
          </Box>
        </CardContent>
      </Card>

      {/* Payment Test Section (Optional - for development) */}
      <Card sx={{ mt: 4 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Payment />
            {i18n.language === 'ar' ? 'اختبار الدفع' : 'Payment Test'}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {i18n.language === 'ar'
              ? 'اختبر نظام الدفع (للتطوير فقط)'
              : 'Test payment system (development only)'}
          </Typography>
          <Button
            variant="outlined"
            onClick={handlePaymentTest}
            startIcon={<Payment />}
          >
            {t('pay')} 100 EGP
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
}